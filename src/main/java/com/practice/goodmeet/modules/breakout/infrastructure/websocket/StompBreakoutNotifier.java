package com.practice.goodmeet.modules.breakout.infrastructure.websocket;

import com.practice.goodmeet.modules.breakout.application.BreakoutNotifier;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
class StompBreakoutNotifier implements BreakoutNotifier {

  private final SimpMessagingTemplate messagingTemplate;

  StompBreakoutNotifier(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void roomChanged(UUID participantId, RoomChange change) {
    messagingTemplate.convertAndSend("/topic/participants/" + participantId + "/room", change);
  }
}
