package com.practice.goodmeet.modules.participant.infrastructure.persistence;

import com.practice.goodmeet.modules.participant.domain.Participant;
import com.practice.goodmeet.modules.participant.domain.ParticipantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaParticipantRepository implements ParticipantRepository {

  private final SpringDataParticipantRepository repository;

  JpaParticipantRepository(SpringDataParticipantRepository repository) {
    this.repository = repository;
  }

  @Override
  public Participant save(Participant participant) {
    return toDomain(repository.save(toEntity(participant)));
  }

  @Override
  public Optional<Participant> findById(UUID id) {
    return repository.findById(id).map(JpaParticipantRepository::toDomain);
  }

  @Override
  public Optional<Participant> findByMeetingAndUser(UUID meetingId, UUID userId) {
    return repository
        .findByMeetingIdAndUserId(meetingId, userId)
        .map(JpaParticipantRepository::toDomain);
  }

  private static Participant toDomain(ParticipantEntity entity) {
    return new Participant(
        entity.getId(),
        entity.getMeetingId(),
        entity.getUserId(),
        entity.getDisplayName(),
        entity.getRole(),
        entity.getMediaIdentity(),
        entity.getCreatedAt());
  }

  private static ParticipantEntity toEntity(Participant participant) {
    return new ParticipantEntity(
        participant.id(),
        participant.meetingId(),
        participant.userId(),
        participant.displayName(),
        participant.role(),
        participant.mediaIdentity(),
        participant.createdAt());
  }
}
