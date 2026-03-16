package gaiden.da.chatservice.dto;

import gaiden.da.chatservice.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private String id;
    private String content;
    private String sender;
    private String guildId;
    private String channelId;
    private String recipientId;
    private String type;
    private List<AttachmentDto> attachments;
    private Map<String, Set<String>> reactions;
    private LocalDateTime timestamp;
    private String action;

}