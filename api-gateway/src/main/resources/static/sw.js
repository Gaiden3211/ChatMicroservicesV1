// Слухаємо подію "push", яку надсилає браузер (Google/Mozilla), коли отримує сигнал від нашого бекенду
self.addEventListener('push', function(event) {
    if (event.data) {
        try {
            // Очікуємо, що бекенд пришле JSON з полями title, body, icon
            const data = event.data.json();

            const options = {
                body: data.body,
                icon: data.icon || 'https://discord.com/assets/f9e7949365287f717887.png', // Дефолтна іконка
                badge: 'https://discord.com/assets/f9e7949365287f717887.png',
                vibrate: [100, 50, 100], // Вібрація для телефонів
                data: { url: data.url || '/' } // Зберігаємо URL, щоб відкрити його при кліку
            };

            // Показуємо нативне сповіщення операційної системи
            event.waitUntil(
                self.registration.showNotification(data.title, options)
            );
        } catch (e) {
            // Якщо прийшов звичайний текст, а не JSON
            event.waitUntil(
                self.registration.showNotification("Нове повідомлення", { body: event.data.text() })
            );
        }
    }
});

// Слухаємо клік по сповіщенню
self.addEventListener('notificationclick', function(event) {
    event.notification.close(); // Закриваємо пуш

    // Відкриваємо вкладку з чатом або переходимо на потрібний URL
    const urlToOpen = event.notification.data.url;
    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(windowClients) {
            // Якщо вкладка вже відкрита - просто фокусуємось на ній
            for (let i = 0; i < windowClients.length; i++) {
                const client = windowClients[i];
                if (client.url === urlToOpen && 'focus' in client) {
                    return client.focus();
                }
            }
            // Якщо закрито - відкриваємо нову
            if (clients.openWindow) {
                return clients.openWindow(urlToOpen);
            }
        })
    );
});