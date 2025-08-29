package org.example.service;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SpeechToTextService {

  public static final String NOTEXT_AVAILABLE = "Notext available.";
  public static final String WHISPER_1 = "whisper-1";
  public static final String MODEL = "model";
  public static final String FILE = "file";
  public static final String TEXT = "text";
  private final RestTemplate restTemplate = new RestTemplate();

  String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

  @Value("${spring.ai.openai.api-key}")
  String OPENAI_API_KEY;

  public String audioToText(MultipartFile file) {

    // Prepare multipart request
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    try {
      body.add(FILE, new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
    } catch (IOException e) {
      return NOTEXT_AVAILABLE;
    }
    body.add(MODEL, WHISPER_1);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(OPENAI_API_KEY);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(WHISPER_URL, requestEntity, Map.class);
    return response.getBody().get(TEXT).toString();


  }
}
