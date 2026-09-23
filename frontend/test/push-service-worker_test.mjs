import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../web/push-service-worker.js', import.meta.url), 'utf8');
const slotId = '00000000-0000-0000-0000-000000000123';
const interestA = '00000000-0000-0000-0000-000000000124';
const interestB = '00000000-0000-0000-0000-000000000125';
const targetUrl = `http://localhost:3000/?screen=appointments&availableSlotId=${slotId}&availabilityInterestId=${interestA}`;

async function createWorker({ windows = [], openWindow }) {
  const listeners = new Map();
  const shown = [];
  let skippedWaiting = false;
  const context = {
    URL,
    console: { info() {}, warn() {}, error() {} },
    self: {
      addEventListener(type, listener) { listeners.set(type, listener); },
      skipWaiting: async () => { skippedWaiting = true; },
      registration: { showNotification: async (title, options) => shown.push({ title, options }) },
      location: { origin: 'http://localhost:3000' },
    },
    clients: {
      matchAll: async (options) => {
        assert.equal(options.type, 'window');
        assert.equal(options.includeUncontrolled, true);
        return windows;
      },
      openWindow,
    },
  };
  vm.runInNewContext(source, context);
  return {
    shown,
    async click(availabilityInterestId = interestA) {
      let work;
      let closed = false;
      listeners.get('notificationclick')({
        notification: {
          data: { availableSlotId: slotId, availabilityInterestId },
          close: () => { closed = true; },
        },
        waitUntil: (promise) => { work = promise; },
      });
      await assert.doesNotReject(work);
      return { closed };
    },
    async push(availabilityInterestId = interestA) {
      let work;
      listeners.get('push')({
        data: { json: () => ({
          title: 'Barber SaaS',
          body: 'Notificação de teste recebida com sucesso.',
          availableSlotId: slotId,
          availabilityInterestId,
        }) },
        waitUntil: (promise) => { work = promise; },
      });
      await work;
    },
    async install() {
      let work;
      listeners.get('install')({ waitUntil: (promise) => { work = promise; } });
      await work;
      return skippedWaiting;
    },
  };
}

{
  const navigated = [];
  let focused = false;
  const appWindow = {
    url: 'http://localhost:3000/?screen=appointments',
    navigate: async (url) => { navigated.push(url); return appWindow; },
    focus: async () => { focused = true; },
  };
  const worker = await createWorker({
    windows: [appWindow],
    openWindow: async () => assert.fail('should not open another window'),
  });

  assert.deepEqual(await worker.click(), { closed: true });
  assert.deepEqual(navigated, [targetUrl]);
  assert.equal(focused, true);
}

for (const invalidNavigation of [
  async () => { throw new Error('navigate failed'); },
  async () => null,
]) {
  let openedTo;
  const worker = await createWorker({
    windows: [{
      url: 'http://localhost:3000/',
      navigate: invalidNavigation,
      focus: async () => assert.fail('should not focus an invalid navigation result'),
    }],
    openWindow: async (url) => { openedTo = url; },
  });

  assert.deepEqual(await worker.click(), { closed: true });
  assert.equal(openedTo, targetUrl);
}

{
  let openedTo;
  const appWindow = {
    url: 'http://localhost:3000/',
    navigate: async () => appWindow,
    focus: async () => { throw new Error('focus failed'); },
  };
  const worker = await createWorker({
    windows: [appWindow],
    openWindow: async (url) => { openedTo = url; },
  });

  assert.deepEqual(await worker.click(), { closed: true });
  assert.equal(openedTo, targetUrl);
}

{
  let openedTo;
  const worker = await createWorker({
    openWindow: async (url) => { openedTo = url; },
  });

  assert.deepEqual(await worker.click(), { closed: true });
  assert.equal(openedTo, targetUrl);
}

{
  const worker = await createWorker({ openWindow: async () => {} });
  assert.equal(await worker.install(), true);
}

{
  const worker = await createWorker({ openWindow: async () => {} });
  await worker.push();
  assert.equal(worker.shown.length, 1);
  assert.equal(worker.shown[0].options.tag, undefined);
  assert.equal(worker.shown[0].options.data.availableSlotId, slotId);
  assert.equal(worker.shown[0].options.data.availabilityInterestId, interestA);
}

{
  const worker = await createWorker({ openWindow: async () => {} });
  await worker.push(interestA);
  await worker.push(interestB);
  assert.equal(worker.shown.length, 2);
  assert.equal(worker.shown[0].options.tag, undefined);
  assert.equal(worker.shown[1].options.tag, undefined);
  assert.notEqual(
    worker.shown[0].options.data.availabilityInterestId,
    worker.shown[1].options.data.availabilityInterestId,
  );
}
