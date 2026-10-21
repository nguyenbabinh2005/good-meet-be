package com.practice.goodmeet.modules.chat.api;

import com.practice.goodmeet.modules.chat.application.ChatService;
import com.practice.goodmeet.shared.ApiHeaders;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ChatController {

  record SendRequest(UUID participantId, String content) {}

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;

  ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
    this.chatService = chatService;
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/meetings/{code}/chat")
  void send(@DestinationVariable String code, @Payload SendRequest request) {
    ChatService.MessageView message =
        chatService.send(code, request.participantId(), request.content());
    messagingTemplate.convertAndSend("/topic/meetings/" + message.meetingId() + "/chat", message);
  }

  @GetMapping("/meetings/{code}/chat/messages")
  List<ChatService.MessageView> history(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    return chatService.history(code, participantId);
  }
}
