package gaiden.da.chatservice.entity;

import gaiden.da.chatservice.dto.AttachmentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String sender;

    private String guildId;
    private String channelId;


    private String recipientId;

    private LocalDateTime timestamp;



     @Column(columnDefinition = "TEXT")
     private String attachments;
}
