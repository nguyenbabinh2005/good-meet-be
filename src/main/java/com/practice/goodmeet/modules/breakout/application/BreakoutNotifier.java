package com.practice.goodmeet.modules.breakout.application;

import java.time.Instant;
import java.util.UUID;

public interface BreakoutNotifier {

  void roomChanged(UUID participantId, RoomChange change);

  record RoomChange(
      String type,
      UUID breakoutRoomId,
      String roomName,
      String mediaUrl,
      String mediaToken,
      Instant mediaTokenExpiresAt) {}
}
