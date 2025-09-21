package com.example.backend.util;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class ImgBBService {

    private static final String API_KEY = "9c790826115d6d8660df304a5aeb1390";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload?key=" + API_KEY;

    public String uploadImage(byte[] imageBytes) {
        RestTemplate restTemplate = new RestTemplate();

        // Encode image to Base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                UPLOAD_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String bodyStr = response.getBody();
            // crude extraction (better: use Jackson/JsonNode)
            String marker = "\"url\":\"";
            int start = bodyStr.indexOf(marker);
            if (start > 0) {
                int end = bodyStr.indexOf("\"", start + marker.length());
                return bodyStr.substring(start + marker.length(), end).replace("\\/", "/");
            }
        }
        throw new RuntimeException("Image upload failed: " + response);
    }
}
