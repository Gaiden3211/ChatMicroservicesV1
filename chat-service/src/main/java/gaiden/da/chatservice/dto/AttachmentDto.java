package gaiden.da.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentDto {
    private String url;          // Посилання на оригінал
    private String thumbnailUrl; // Посилання на прев'ю (може бути null для файлів)
    private String fileName;     // Назва файлу (document.pdf)
    private String type;         // IMAGE, FILE
}
