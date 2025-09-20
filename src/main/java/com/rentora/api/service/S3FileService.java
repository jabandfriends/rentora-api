package com.rentora.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Presigner presigner; // inject S3Presigner, not S3Client
    private final S3Client s3Client;     // for S3 operations like delete
    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    // Generate GET presigned URL (download)
    public URL generatePresignedUrlForGet(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(10)) // URL valid for 10 minutes
                .build();

        return presigner.presignGetObject(presignRequest).url();
    }

    // Generate PUT presigned URL (upload)
    public URL generatePresignedUrlForPut(String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        return presigner.presignPutObject(presignRequest).url();
    }

    // Delete a file from S3
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
