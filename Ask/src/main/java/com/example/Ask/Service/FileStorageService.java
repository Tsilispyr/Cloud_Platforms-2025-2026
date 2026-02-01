package com.example.Ask.Service;

import com.example.Ask.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @PostConstruct
    public void initBucket() {
        try {
            String bucketName = minioConfig.getBucket();
            String endpoint = minioConfig.getEndpoint();
            System.out.println("=== Initializing MinIO Bucket ===");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Bucket: " + bucketName);
            
            // Test connection
            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    System.out.println("Bucket does not exist, creating...");
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    System.out.println("✓ MinIO bucket created: " + bucketName);
                } else {
                    System.out.println("✓ MinIO bucket already exists: " + bucketName);
                }
            } catch (Exception e) {
                System.err.println("✗ Error checking/creating bucket: " + e.getMessage());
                System.err.println("Error type: " + e.getClass().getName());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("✗ CRITICAL: Error initializing MinIO bucket: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - let the app start but log the error
        }
    }

    public String uploadImage(MultipartFile file) {
        try {
            // Ensure bucket exists
            String bucketName = minioConfig.getBucket();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("MinIO bucket created during upload: " + bucketName);
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("Invalid filename: " + originalFilename);
            }
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            System.out.println("Uploading file to MinIO: bucket=" + bucketName + ", filename=" + filename + ", size=" + file.getSize());
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            System.out.println("File uploaded successfully: " + filename);
            return filename;
        } catch (Exception e) {
            System.err.println("Failed to upload image: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public String generatePublicUrl(String filename) {
        try {
            // Generate presigned URL for public access (valid for 7 days)
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucket())
                    .object(filename)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate public URL: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String filename) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(filename)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    public boolean imageExists(String filename) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(filename)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public InputStream getImageInputStream(String filename) throws Exception {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(filename)
                .build()
        );
    }
} 