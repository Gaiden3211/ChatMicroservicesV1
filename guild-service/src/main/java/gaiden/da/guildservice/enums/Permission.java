package gaiden.da.guildservice.enums;


public enum Permission {
    ADMINISTRATOR,       // Повний доступ
    MANAGE_GUILD,        // Змінювати назву, іконку
    MANAGE_ROLES,        // Створювати/видаляти ролі
    MANAGE_CHANNELS,     // Створювати/видаляти канали
    KICK_MEMBERS,        // Виганяти учасників
    BAN_MEMBERS,         // Банити учасників
    SEND_MESSAGES,       // Писати в чат
    READ_MESSAGES,       // Читати чат
    CONNECT_VOICE,        // Підключатися до голосових каналів
    CREATE_INSTANT_INVITE
}
