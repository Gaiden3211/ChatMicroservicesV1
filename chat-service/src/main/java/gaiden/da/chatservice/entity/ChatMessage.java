package gaiden.da.chatservice.entity;

import gaiden.da.chatservice.dto.AttachmentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {


    @Id
    private String id;


    private String content;

    private String sender;

    private String guildId;
    private String channelId;


    private String recipientId;

    private LocalDateTime timestamp;



    private List<AttachmentDto> attachments;
    private Map<String, Set<String>> reactions;
}
