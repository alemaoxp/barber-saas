import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../web/push_test_client.js', import.meta.url), 'utf8');

async function subscribe({ existing, unsubscribeResult = true }) {
  const calls = [];
  const fresh = {
    toJSON: () => ({ endpoint: 'new', keys: { p256dh: 'new-key', auth: 'new-auth' } }),
  };
  const registration = {
    pushManager: {
      getSubscription: async () => {
        calls.push('get');
        return existing && {
          toJSON: () => ({ endpoint: 'old' }),
          unsubscribe: async () => {
            calls.push('unsubscribe');
            return unsubscribeResult;
          },
        };
      },
      subscribe: async () => {
        calls.push('subscribe');
        return fresh;
      },
    },
  };
  const notification = { requestPermission: async () => 'granted' };
  const context = {
    window: { PushManager: function PushManager() {}, Notification: notification },
    navigator: { serviceWorker: { register: async () => registration } },
    Notification: notification,
    atob,
    Uint8Array,
  };
  vm.runInNewContext(source, context);
  return { calls, run: () => context.window.barberPushTest.subscribe('AQ') };
}

{
  const test = await subscribe({ existing: true });
  assert.deepEqual(JSON.parse(await test.run()), {
    endpoint: 'new', keys: { p256dh: 'new-key', auth: 'new-auth' },
  });
  assert.deepEqual(test.calls, ['get', 'unsubscribe', 'subscribe']);
}

{
  const test = await subscribe({ existing: false });
  await test.run();
  assert.deepEqual(test.calls, ['get', 'subscribe']);
}

{
  const test = await subscribe({ existing: true, unsubscribeResult: false });
  await assert.rejects(test.run(), /Não foi possível remover a subscription anterior/);
  assert.deepEqual(test.calls, ['get', 'unsubscribe']);
}
