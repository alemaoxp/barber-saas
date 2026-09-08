self.addEventListener('push', (event) => {
  const payload = event.data ? event.data.json() : {};
  event.waitUntil(self.registration.showNotification(payload.title || 'Barber SaaS', {
    body: payload.body || 'Notificação de teste recebida com sucesso.',
  }));
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const targetUrl = `${self.location.origin}/?screen=appointments`;
  event.waitUntil((async () => {
    try {
      const windows = await clients.matchAll({type: 'window', includeUncontrolled: true});
      console.info('notificationclick: windows found', windows.length);
      const appWindow = windows.find((window) =>
        new URL(window.url).origin === self.location.origin);
      if (!appWindow) {
        console.info('notificationclick: openWindow');
        await clients.openWindow(targetUrl);
        return;
      }

      let navigatedWindow;
      try {
        console.info('notificationclick: navigate');
        navigatedWindow = await appWindow.navigate(targetUrl);
        console.info('notificationclick: navigate succeeded');
      } catch (error) {
        console.warn('notificationclick: navigate failed; using fallback', error);
      }
      if (!navigatedWindow || typeof navigatedWindow.focus !== 'function') {
        console.warn('notificationclick: invalid navigation result; using fallback');
        console.info('notificationclick: openWindow fallback');
        await clients.openWindow(targetUrl);
        return;
      }
      try {
        await navigatedWindow.focus();
        console.info('notificationclick: focus succeeded');
      } catch (error) {
        console.warn('notificationclick: focus failed; using fallback', error);
        console.info('notificationclick: openWindow fallback');
        await clients.openWindow(targetUrl);
      }
    } catch (error) {
      console.error('notificationclick: final failure', error);
    }
  })());
});
