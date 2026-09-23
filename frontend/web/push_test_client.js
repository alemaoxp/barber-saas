window.barberPushTest = {
  subscribe: async (publicKey) => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
      throw new Error('Este navegador não suporta notificações Web Push.');
    }
    const permission = Notification.permission === 'default'
      ? await Notification.requestPermission()
      : Notification.permission;
    if (permission !== 'granted') {
      throw new Error('Permissão de notificação não foi concedida.');
    }
    const registration = await navigator.serviceWorker.register(
      'push-service-worker.js?v=3',
      {scope: 'push/', updateViaCache: 'none'},
    );
    const existingSubscription = await registration.pushManager.getSubscription();
    const reusableSubscription = existingSubscription &&
      sameApplicationServerKey(existingSubscription, publicKey)
      ? existingSubscription
      : null;
    if (existingSubscription && reusableSubscription === null) {
      if (!await existingSubscription.unsubscribe()) {
        throw new Error('Não foi possível atualizar a subscription Web Push.');
      }
    }
    const subscription = reusableSubscription || await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: base64UrlToUint8Array(publicKey),
    });
    return JSON.stringify(subscription.toJSON());
  },
};

function base64UrlToUint8Array(value) {
  const padding = '='.repeat((4 - value.length % 4) % 4);
  const binary = atob((value + padding).replace(/-/g, '+').replace(/_/g, '/'));
  return Uint8Array.from(binary, (character) => character.charCodeAt(0));
}

function sameApplicationServerKey(subscription, publicKey) {
  const existingKey = subscription.options?.applicationServerKey;
  const expectedKey = base64UrlToUint8Array(publicKey);
  const actualKey = existingKey && new Uint8Array(existingKey);
  return actualKey?.length === expectedKey.length &&
    actualKey.every((byte, index) => byte === expectedKey[index]);
}
