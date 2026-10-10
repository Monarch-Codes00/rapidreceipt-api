package com.rapidreceipt.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.url:https://bjcuqajhynqgagxkoknc.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.bucket:logos}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadLogo(MultipartFile file, Long userId) throws IOException {
        byte[] bytes = file.getBytes();
        String contentType = file.getContentType() != null ? file.getContentType() : "image/png";

        // If supabaseKey is provided, upload directly to Supabase Storage Bucket
        if (supabaseKey != null && !supabaseKey.isBlank()) {
            try {
                String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
                String extension = ".png";
                if (originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = "logo_user_" + userId + "_" + UUID.randomUUID().toString() + extension;
                String uploadEndpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + supabaseKey);
                headers.set("apikey", supabaseKey);
                headers.setContentType(MediaType.parseMediaType(contentType));

                HttpEntity<byte[]> entity = new HttpEntity<>(bytes, headers);
                ResponseEntity<String> response = restTemplate.exchange(uploadEndpoint, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                    log.info("Successfully uploaded logo to Supabase Storage: {}", publicUrl);
                    return publicUrl;
                } else {
                    log.warn("Supabase Storage upload returned status: {}, falling back to Base64", response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Failed to upload logo to Supabase Storage: {}, falling back to Base64", e.getMessage());
            }
        }

        // Graceful fallback: return self-contained Base64 Data URI if Supabase key is not yet configured or fails
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
