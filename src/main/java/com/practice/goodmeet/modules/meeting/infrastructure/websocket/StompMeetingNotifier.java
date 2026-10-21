package com.practice.goodmeet.modules.meeting.infrastructure.websocket;

import com.practice.goodmeet.modules.meeting.application.MeetingNotifier;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
class StompMeetingNotifier implements MeetingNotifier {

  private final SimpMessagingTemplate messagingTemplate;

  StompMeetingNotifier(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void waitingRoomChanged(UUID meetingId, WaitingRoomEvent event) {
    messagingTemplate.convertAndSend("/topic/meetings/" + meetingId + "/waiting-room", event);
  }

  @Override
  public void joinRequestDecided(UUID requestId, JoinDecision decision) {
    messagingTemplate.convertAndSend("/topic/join-requests/" + requestId, decision);
  }
}
