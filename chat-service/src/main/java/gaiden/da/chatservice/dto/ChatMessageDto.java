package gaiden.da.chatservice.dto;

import gaiden.da.chatservice.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private String content;
    private String sender;
    private String guildId;
    private String channelId;
    private String recipientId;
    private MessageType type;
    private List<AttachmentDto> attachments;
    private LocalDateTime timestamp;

}