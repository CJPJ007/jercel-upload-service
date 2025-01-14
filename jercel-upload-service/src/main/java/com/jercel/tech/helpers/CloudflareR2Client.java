package com.jercel.tech.helpers;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jercel.tech.service.RedisPushService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for interacting with Cloudflare R2 Storage using AWS SDK S3
 * compatibility
 */
@Service
@Slf4j
public class CloudflareR2Client {

    /**
     * Configuration class for R2 credentials and endpoint
     */
    @Value("${r2.api.access.key}")
    private String accessKey;
    @Value("${r2.api.secret.key}")
    private String secretKey;
    @Value("${r2.endpoint.url}")
    private String endpoint;
    @Value("${bucket.name}")
    private String bucketName;

    private S3Client s3Client;
    @Autowired
    RedisPushService redisPushService;

    /**
     * Builds and configures the S3 client with R2-specific settings
     */
    @PostConstruct
    private void buildS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey);

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    /**
     * Lists all objects in the specified bucket
     */
    public List<S3Object> listObjects(String bucketName) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            return s3Client.listObjectsV2(request).contents();
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to list objects in bucket " + bucketName + ": " + e.getMessage(), e);
        }
    }

    public String uploadFolder(Path sourceFolder) throws IOException {
        String folderName = sourceFolder.getFileName().toString() + generateRandomId();
        List<Path> paths = Files.walk(sourceFolder).filter(Files::isRegularFile).collect(Collectors.toList());
        paths.parallelStream().forEach((path) -> {
            try {
                uploadFile(sourceFolder, path, folderName);
            } catch (Exception e) {
                log.error("Exception in uploadfile for " + path, e);
            }
        });
        redisPushService.enqueueTask(folderName);
        redisPushService.insertDeploymentStatus(folderName);
        return folderName;
        // Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {

        // @Override
        // public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        // try {
        // uploadFile(sourceFolder, file, folderName);
        // } catch (IOException e) {
        // log.error("Failed to upload file: ", e);
        // }
        // return FileVisitResult.CONTINUE;
        // }

        // // Optionally handle symbolic links, errors, etc.

        // });
    }

    private String generateRandomId() {
        Random random = new Random();
        StringBuilder id = new StringBuilder("-");
        // Add 4 random lowercase letters
        for (int i = 0; i < 4; i++) {
            id.append((char) (random.nextInt(26) + 'a'));
        }
        // Add 1 random number
        id.append(random.nextInt(10));
        return id.toString();
    }

    private void uploadFile(Path sourceFolder, Path filePath, String sourceFolderName) throws IOException {
        // Compute the relative path to preserve folder structure in GCS
        // log.info("filePath : {} currentPath : {}", filePath,
        // Paths.get("").toAbsolutePath());
        Path relativePath = sourceFolder.relativize(filePath);
        // log.info("Relative Path : {}", relativePath.toString().startsWith("."));
        if (relativePath.toString().startsWith("."))
            return;
        String objectName = sourceFolderName + "/"
                + relativePath.toString().replace(FileSystems.getDefault().getSeparator(), "/"); // GCS uses '/' as
                                                                                                 // separator

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        // Upload the file
        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(filePath));
        // log.info("Uploaded Blob : {}", uploadedBlob.getBlobId());
        // log.info("Uploaded : {}", objectName);
    }

}