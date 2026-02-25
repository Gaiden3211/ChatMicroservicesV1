package gaiden.da.chatservice.controller;

import gaiden.da.chatservice.dto.AttachmentDto;
import gaiden.da.chatservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;


    @PostMapping("/upload/files")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Отримав файл: {}", file.getOriginalFilename());
            AttachmentDto result = mediaService.processAndUpload(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Помилка завантаження", e);
            return ResponseEntity.status(500).body(Map.of("error", "Помилка при обробці файлу"));
        }
    }
}
