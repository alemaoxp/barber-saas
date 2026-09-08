window.barberPushTest = {
  subscribe: async (publicKey) => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
      throw new Error('Este navegador não suporta notificações Web Push.');
    }
    if (await Notification.requestPermission() !== 'granted') {
      throw new Error('Permissão de notificação não foi concedida.');
    }
    const registration = await navigator.serviceWorker.register('push-service-worker.js', {scope: 'push/'});
    const existingSubscription = await registration.pushManager.getSubscription();
    if (existingSubscription && !await existingSubscription.unsubscribe()) {
      throw new Error('Não foi possível remover a subscription anterior.');
    }
    const subscription = await registration.pushManager.subscribe({
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
