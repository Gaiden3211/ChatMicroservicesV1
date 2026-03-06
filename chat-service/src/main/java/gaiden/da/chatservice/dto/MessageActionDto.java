package gaiden.da.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageActionDto {
    private String messageId;
    private String action;
    private String payload;

    private String senderId;


    private String recipientId;
    private String guildId;
    private String channelId;
}
