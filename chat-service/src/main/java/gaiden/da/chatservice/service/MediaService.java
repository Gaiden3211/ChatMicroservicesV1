package gaiden.da.chatservice.service;

import gaiden.da.chatservice.dto.AttachmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final S3Service s3Service;

    public AttachmentDto processAndUpload(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();


        String safeName = uuid + "_" + (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "file");

        byte[] originalBytes = file.getBytes();
        String originalUrl = s3Service.uploadFile(safeName, originalBytes, contentType);
        String thumbnailUrl = null;
        String type = "FILE";


        if (contentType != null && contentType.startsWith("image/")) {
            type = "IMAGE";
            try {
                String thumbFileName = "thumb_" + uuid + ".jpg";
                ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();

                Thumbnails.of(file.getInputStream())
                        .size(300, 300)
                        .outputFormat("jpg")
                        .toOutputStream(thumbOutput);

                byte[] thumbBytes = thumbOutput.toByteArray();
                thumbnailUrl = s3Service.uploadFile(thumbFileName, thumbBytes, "image/jpeg");
            } catch (Exception e) {
                log.error("Не вдалося створити прев'ю для {}: {}", originalFilename, e.getMessage());

            }
        }else if (contentType.startsWith("audio/")) {
            type = "VOICE";

        }

        return AttachmentDto.builder()
                .url(originalUrl)
                .thumbnailUrl(thumbnailUrl)
                .fileName(originalFilename)
                .type(type)
                .build();
    }
}
