package com.practice.goodmeet.modules.breakout.infrastructure.persistence;

import com.practice.goodmeet.modules.breakout.domain.BreakoutRoom;
import com.practice.goodmeet.modules.breakout.domain.BreakoutRoomRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaBreakoutRoomRepository implements BreakoutRoomRepository {

  private final SpringDataBreakoutRoomRepository repository;

  JpaBreakoutRoomRepository(SpringDataBreakoutRoomRepository repository) {
    this.repository = repository;
  }

  @Override
  public BreakoutRoom save(BreakoutRoom room) {
    return toDomain(
        repository.save(
            new BreakoutRoomEntity(
                room.id(),
                room.meetingId(),
                room.sessionId(),
                room.name(),
                room.mediaRoomId(),
                room.createdAt(),
                room.closedAt())));
  }

  @Override
  public Optional<BreakoutRoom> findById(UUID id) {
    return repository.findById(id).map(JpaBreakoutRoomRepository::toDomain);
  }

  @Override
  public List<BreakoutRoom> findOpenBySession(UUID sessionId) {
    return repository.findBySessionIdAndClosedAtIsNull(sessionId).stream()
        .map(JpaBreakoutRoomRepository::toDomain)
        .toList();
  }

  private static BreakoutRoom toDomain(BreakoutRoomEntity entity) {
    return new BreakoutRoom(
        entity.getId(),
        entity.getMeetingId(),
        entity.getSessionId(),
        entity.getName(),
        entity.getMediaRoomId(),
        entity.getCreatedAt(),
        entity.getClosedAt());
  }
}
