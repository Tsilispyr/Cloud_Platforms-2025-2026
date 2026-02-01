package com.example.Ask.Controllers;

import com.example.Ask.Entities.Animal;
import com.example.Ask.Service.AnimalService;
import com.example.Ask.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import com.example.Ask.config.MinioConfig;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AnimalService animalService;
    
    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private MinioConfig minioConfig;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("UPLOAD: /upload endpoint called, file=" + (file != null ? file.getOriginalFilename() : "null"));
        try {
            String imageUrl = fileStorageService.uploadImage(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload-animal-image/{animalId}")
    public ResponseEntity<Map<String, String>> uploadAnimalImage(
            @PathVariable Integer animalId,
            @RequestParam("file") MultipartFile file) {
        System.out.println("UPLOAD: /upload-animal-image endpoint called, animalId=" + animalId + ", file=" + (file != null ? file.getOriginalFilename() : "null"));
        
        // Validate file
        if (file == null || file.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "File is required");
            System.err.println("UPLOAD ERROR: File is null or empty");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "File size exceeds 10MB limit");
            System.err.println("UPLOAD ERROR: File size too large: " + file.getSize());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Upload image to MinIO
            System.out.println("UPLOAD: Attempting to upload file to MinIO...");
            String filename = fileStorageService.uploadImage(file);
            System.out.println("UPLOAD: File uploaded successfully, filename: " + filename);

            // Update animal record with image filename
            Animal animal = animalService.getAnimal(animalId);
            if (animal != null) {
                animal.setImageUrl(filename);
                animalService.saveAnimal(animal);
                System.out.println("UPLOAD: Animal updated with image URL: " + filename);

                Map<String, String> response = new HashMap<>();
                response.put("imageUrl", filename);
                response.put("message", "Animal image uploaded successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Animal not found with id: " + animalId);
                System.err.println("UPLOAD ERROR: Animal not found with id: " + animalId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("UPLOAD ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload animal image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/image/{filename}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        try {
            System.out.println("GET IMAGE: Requested filename: " + filename);
            
            // Get object from MinIO
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(filename)
                    .build()
            );
            
            // Determine content type
            String contentType = "image/jpeg";
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }
            
            System.out.println("GET IMAGE: Returning image with content type: " + contentType);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(new InputStreamResource(stream));
        } catch (Exception e) {
            System.err.println("GET IMAGE ERROR: Failed to get image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        try {
            fileStorageService.deleteImage(filename);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 