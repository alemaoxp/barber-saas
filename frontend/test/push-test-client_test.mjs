import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';

const source = await readFile(new URL('../web/push_test_client.js', import.meta.url), 'utf8');

async function subscribe({ existing, unsubscribeResult = true }) {
  const calls = [];
  let registered;
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
  const notification = { permission: 'default', requestPermission: async () => 'granted' };
  const context = {
    window: { PushManager: function PushManager() {}, Notification: notification },
    navigator: { serviceWorker: { register: async (script, options) => {
      registered = { script, options };
      return registration;
    } } },
    Notification: notification,
    atob,
    Uint8Array,
  };
  vm.runInNewContext(source, context);
  return { calls, registered: () => registered,
    run: () => context.window.barberPushTest.subscribe('AQ') };
}

{
  const test = await subscribe({ existing: true });
  assert.deepEqual(JSON.parse(await test.run()), {
    endpoint: 'new', keys: { p256dh: 'new-key', auth: 'new-auth' },
  });
  assert.deepEqual(test.calls, ['get', 'unsubscribe', 'subscribe']);
  assert.equal(test.registered().script, 'push-service-worker.js?v=3');
  assert.equal(test.registered().options.scope, 'push/');
  assert.equal(test.registered().options.updateViaCache, 'none');
}

{
  const test = await subscribe({ existing: false });
  await test.run();
  assert.deepEqual(test.calls, ['get', 'subscribe']);
}

{
  const test = await subscribe({ existing: true, unsubscribeResult: false });
  await assert.rejects(test.run(), /Não foi possível atualizar a subscription Web Push/);
  assert.deepEqual(test.calls, ['get', 'unsubscribe']);
}

{
  let permissionRequests = 0;
  let subscriptions = 0;
  const existing = {
    options: { applicationServerKey: Uint8Array.from([1, 2, 3]) },
    toJSON: () => ({ endpoint: 'existing', keys: { p256dh: 'key', auth: 'auth' } }),
  };
  const notification = {
    permission: 'granted',
    requestPermission: async () => {
      permissionRequests++;
      return 'granted';
    },
  };
  const context = {
    window: { PushManager: function PushManager() {}, Notification: notification },
    navigator: { serviceWorker: { register: async () => ({
      pushManager: {
        getSubscription: async () => existing,
        subscribe: async () => {
          subscriptions++;
          return existing;
        },
      },
    }) } },
    Notification: notification,
    atob,
    Uint8Array,
  };
  vm.runInNewContext(source, context);

  assert.deepEqual(JSON.parse(await context.window.barberPushTest.subscribe('AQID')), existing.toJSON());
  assert.equal(permissionRequests, 0);
  assert.equal(subscriptions, 0);
}
