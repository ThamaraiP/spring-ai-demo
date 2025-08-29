package org.example.controller;

import java.io.ByteArrayInputStream;
import org.example.service.SpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/speech")
public class SpeechController {

  @Autowired
  private SpeechService speechService;

  @GetMapping(value = "/speak", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<Resource> speak(@RequestParam(value = "message") String text) {
    // Call the model
    byte[] audioBytes = speechService.textToSpeech(text);

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=output.mp3")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(new ByteArrayInputStream(audioBytes)));
  }

  @PostMapping("/chat")
  public ResponseEntity<ByteArrayResource> chatWithAudio(@RequestParam("file") MultipartFile file) {
    try {
      // 1. Convert audio to text
      String userText = speechService.speechToText(file);

      // 2. Send text to chat AI
      String aiResponse = speechService.chat(userText);

      // 3. Convert AI response back to audio
      byte[] audioBytes = speechService.textToSpeech(aiResponse);

      ByteArrayResource resource = new ByteArrayResource(audioBytes);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=response.mp3")
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(resource);

    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
