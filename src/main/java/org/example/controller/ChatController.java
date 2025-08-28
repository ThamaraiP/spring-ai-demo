package org.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;

@RestController
@RequestMapping("/chat")
public class ChatController {

  private final ChatModel chatModel;

  public ChatController(ChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @GetMapping("/ask")
  public String ask(@RequestParam(value = "message") String message) {
    Prompt prompt = new Prompt(new UserMessage(message));
    ChatResponse response = chatModel.call(prompt);
    return response.getResult().getOutput().toString();
  }
}

