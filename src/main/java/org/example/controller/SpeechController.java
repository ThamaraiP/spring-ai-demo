package org.example.controller;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/speech")
public class SpeechController {

  private final SpeechModel speechModel;

  public SpeechController(SpeechModel speechModel) {
    this.speechModel = speechModel;
  }

  @GetMapping(value = "/speak", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<Resource> speak(@RequestParam(value = "message") String text) {
    // Create prompt for speech
    SpeechPrompt prompt = new SpeechPrompt(text);

    // Call the model
    SpeechResponse response = speechModel.call(prompt);

    // Extract audio bytes
    byte[] audioBytes = response.getResult().getOutput();

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=output.mp3")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(new ByteArrayInputStream(audioBytes)));
  }
}
