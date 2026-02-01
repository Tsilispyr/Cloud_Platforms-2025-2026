package com.example.Ask.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class ThingsboardService {

    @Value("${THINGSBOARD_URL:http://thingsboard:8080}")
    private String thingsboardUrl;

    @Value("${THINGSBOARD_TOKEN:}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Scheduled(fixedRate = 30000)
    public void sendTelemetry() {
        if (accessToken == null || accessToken.isEmpty()) {
            return;
        }

        try {
            String url = thingsboardUrl + "/api/v1/" + accessToken + "/telemetry";

            System.out.println("🔄 [Thingsboard] Sending telemetry to: " + thingsboardUrl);
            Map<String, Object> data = new HashMap<>();
            data.put("temperature", 37.5 + (random.nextDouble() * 2));
            data.put("heartRate", 60 + random.nextInt(60));
            data.put("activityLevel", random.nextInt(10));
            data.put("batteryLevel", 100 - random.nextInt(20));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ [Thingsboard] Telemetry sent successfully");
            
        } catch (Exception e) {
            System.err.println("⚠️ [Thingsboard] Failed to send telemetry: " + e.getMessage());
        }
    }

    /**
     * Sends animal data to ThingsBoard as telemetry
     */
    public void sendAnimalData(String animalName, String animalType, int animalAge, String animalGender, Integer animalId) {
        if (accessToken == null || accessToken.isEmpty()) {
            System.out.println("⚠️ [Thingsboard] Access token not configured, skipping animal data");
            return;
        }

        try {
            String url = thingsboardUrl + "/api/v1/" + accessToken + "/telemetry";

            System.out.println("🔄 [Thingsboard] Sending animal data for: " + animalName);
            Map<String, Object> data = new HashMap<>();
            data.put("animalId", animalId != null ? animalId : 0);
            data.put("animalName", animalName);
            data.put("animalType", animalType);
            data.put("animalAge", animalAge);
            data.put("animalGender", animalGender);
            data.put("timestamp", System.currentTimeMillis());
            
            // Additional metrics that can be used for dashboards
            data.put("isNewAnimal", 1);
            data.put("status", "available");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ [Thingsboard] Animal data sent successfully for: " + animalName);
            
        } catch (Exception e) {
            System.err.println("⚠️ [Thingsboard] Failed to send animal data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}