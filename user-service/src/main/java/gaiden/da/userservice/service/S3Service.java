package gaiden.da.userservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String uploadFile(String originalFilename, byte[] content, String contentType) {
        String key = "avatars/" + UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
        } catch (Exception e) {
            log.error("S3 Upload Error", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
