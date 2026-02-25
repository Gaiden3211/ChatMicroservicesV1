package gaiden.da.chatservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    public String uploadFile(String key, byte[] content, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }

        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }
}
