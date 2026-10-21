package com.practice.goodmeet.modules.breakout.infrastructure.memory;

import com.practice.goodmeet.modules.breakout.domain.BreakoutAssignmentStore;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class InMemoryBreakoutAssignmentStore implements BreakoutAssignmentStore {

  private final Map<UUID, UUID> roomByParticipant = new ConcurrentHashMap<>();

  @Override
  public void assign(UUID participantId, UUID roomId) {
    roomByParticipant.put(participantId, roomId);
  }

  @Override
  public Optional<UUID> findRoom(UUID participantId) {
    return Optional.ofNullable(roomByParticipant.get(participantId));
  }

  @Override
  public void unassign(UUID participantId) {
    roomByParticipant.remove(participantId);
  }

  @Override
  public List<UUID> findParticipants(UUID roomId) {
    return roomByParticipant.entrySet().stream()
        .filter(entry -> entry.getValue().equals(roomId))
        .map(Map.Entry::getKey)
        .toList();
  }

  @Override
  public void clearRoom(UUID roomId) {
    roomByParticipant.values().removeIf(roomId::equals);
  }
}
