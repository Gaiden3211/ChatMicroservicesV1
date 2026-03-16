const { Client } = require('pg');
const { MongoClient } = require('mongodb');

// 1. Твої доступи (ОБОВ'ЯЗКОВО ЗАМІНИ НА СВОЇ!)
const PG_URI = "postgresql://ep-orange-moon-aia3zgw0-pooler.c-4.us-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_GmaFHEW12Xru&sslmode=require&channelBinding=require";
const MONGO_URI = "mongodb://admin:password@localhost:27017"; // Якщо запускаєш на сервері, де піднято докер

async function migrate() {
    console.log("🚀 Починаємо міграцію: PostgreSQL -> MongoDB...");

    const pgClient = new Client({ connectionString: PG_URI });
    const mongoClient = new MongoClient(MONGO_URI);

    try {
        await pgClient.connect();
        await mongoClient.connect();
        console.log("✅ Підключено до обох баз!");

        const mongoDb = mongoClient.db('chat_db'); // Створиться автоматично
        const messagesCollection = mongoDb.collection('messages');
        const contactsCollection = mongoDb.collection('contacts');

        // ==========================================
        // 📦 МІГРАЦІЯ ПОВІДОМЛЕНЬ
        // ==========================================
        console.log("📥 Витягуємо повідомлення з Postgres...");
        const msgsRes = await pgClient.query('SELECT * FROM messages');

        if (msgsRes.rows.length > 0) {
            const mongoMessages = msgsRes.rows.map(row => {
                // Магія трансформації: перетворюємо текст назад у JSON
                let parsedAttachments = [];
                let parsedReactions = {};

                try { if (row.attachments) parsedAttachments = JSON.parse(row.attachments); } catch(e){}
                try { if (row.reactions) parsedReactions = JSON.parse(row.reactions); } catch(e){}

                return {
                    _id: String(row.id), // 🔥 Зберігаємо старий ID як строковий ObjectID!
                    sender: row.sender,
                    content: row.content,
                    guildId: row.guild_id,
                    channelId: row.channel_id,
                    recipientId: row.recipient_id,
                    timestamp: row.timestamp,
                    attachments: parsedAttachments, // Тепер це справжній масив у БД!
                    reactions: parsedReactions      // Тепер це справжній об'єкт у БД!
                };
            });

            // Очищаємо колекцію перед заливкою (на випадок перезапуску скрипта)
            await messagesCollection.deleteMany({});
            await messagesCollection.insertMany(mongoMessages);
            console.log(`✅ Успішно перенесено повідомлень: ${mongoMessages.length}`);
        } else {
            console.log("🤷‍♂️ Повідомлень у базі немає.");
        }

        // ==========================================
        // 📦 МІГРАЦІЯ КОНТАКТІВ (Direct Messages)
        // ==========================================
        console.log("📥 Витягуємо контакти...");
        const contactsRes = await pgClient.query('SELECT * FROM contacts');

        if (contactsRes.rows.length > 0) {
            const mongoContacts = contactsRes.rows.map(row => ({
                _id: String(row.id),
                ownerId: row.owner_id,
                peerId: row.peer_id,
                lastInteraction: row.last_interaction
            }));

            await contactsCollection.deleteMany({});
            await contactsCollection.insertMany(mongoContacts);
            console.log(`✅ Успішно перенесено контактів: ${mongoContacts.length}`);
        }

        console.log("🎉 МІГРАЦІЯ ЗАВЕРШЕНА УСПІШНО!");

    } catch (err) {
        console.error("❌ Помилка міграції:", err);
    } finally {
        await pgClient.end();
        await mongoClient.close();
    }
}

migrate();