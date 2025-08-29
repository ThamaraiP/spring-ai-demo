package org.example.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SpeechService {

  @Autowired
  private ChatClient chatClient;
  @Autowired
  private SpeechToTextService speechToTextService;
  @Autowired private SpeechModel speechModel;

  public String speechToText(MultipartFile audioBytes) {
    return speechToTextService.audioToText(audioBytes);
  }

  public String chat(String inputText) {
    CallResponseSpec call = chatClient.prompt().user(u -> u.text(inputText)).call();
    return  call.chatResponse().getResult().getOutput().getText();
  }

  public byte[] textToSpeech(String text) {
    return speechModel.call(text);
  }
}

