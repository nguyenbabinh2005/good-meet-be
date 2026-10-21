package com.practice.goodmeet.modules.meeting.application;

import java.time.Instant;
import java.util.UUID;

public interface MeetingNotifier {

  void waitingRoomChanged(UUID meetingId, WaitingRoomEvent event);

  void joinRequestDecided(UUID requestId, JoinDecision decision);

  record WaitingRoomEvent(String type, UUID requestId, String displayName, Instant requestedAt) {}

  record JoinDecision(boolean approved, JoinService.JoinResult result) {}
}
