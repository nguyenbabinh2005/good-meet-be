package com.practice.goodmeet.modules.breakout.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BreakoutAssignmentStore {

  void assign(UUID participantId, UUID roomId);

  Optional<UUID> findRoom(UUID participantId);

  void unassign(UUID participantId);

  List<UUID> findParticipants(UUID roomId);

  void clearRoom(UUID roomId);
}
