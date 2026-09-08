import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../web/push-service-worker.js', import.meta.url), 'utf8');
const targetUrl = 'http://localhost:3000/?screen=appointments';

async function createWorker({ windows = [], openWindow }) {
  const listeners = new Map();
  const shown = [];
  const context = {
    URL,
    console: { info() {}, warn() {}, error() {} },
    self: {
      addEventListener(type, listener) { listeners.set(type, listener); },
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
    async click() {
      let work;
      let closed = false;
      listeners.get('notificationclick')({
        notification: { close: () => { closed = true; } },
        waitUntil: (promise) => { work = promise; },
      });
      await assert.doesNotReject(work);
      return { closed };
    },
    async push() {
      let work;
      listeners.get('push')({
        data: { json: () => ({ title: 'Barber SaaS', body: 'Notificação de teste recebida com sucesso.' }) },
        waitUntil: (promise) => { work = promise; },
      });
      await work;
    },
  };
}

{
  const navigated = [];
  let focused = false;
  const appWindow = {
    url: 'http://localhost:3000/',
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
  await worker.push();
  assert.equal(worker.shown.length, 1);
  assert.equal(worker.shown[0].options.tag, undefined);
}
