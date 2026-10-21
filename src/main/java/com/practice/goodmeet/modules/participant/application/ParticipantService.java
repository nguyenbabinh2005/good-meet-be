package com.practice.goodmeet.modules.participant.application;

import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantInfo;
import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import com.practice.goodmeet.modules.participant.domain.Participant;
import com.practice.goodmeet.modules.participant.domain.ParticipantRepository;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class ParticipantService implements ParticipantDirectory {

  private final ParticipantRepository participantRepository;
  private final Clock clock;

  ParticipantService(ParticipantRepository participantRepository, Clock clock) {
    this.participantRepository = participantRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public ParticipantInfo join(
      UUID meetingId, UUID userId, String displayName, ParticipantRole role) {
    if (displayName == null || displayName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name is required");
    }
    if (role == null || (userId == null) != (role == ParticipantRole.GUEST)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Role does not match the participant type");
    }
    if (userId != null) {
      Optional<Participant> existing =
          participantRepository.findByMeetingAndUser(meetingId, userId);
      if (existing.isPresent()) {
        return toInfo(existing.get());
      }
    }
    return toInfo(
        participantRepository.save(
            Participant.create(meetingId, userId, displayName, role, clock.instant())));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ParticipantInfo> findById(UUID participantId) {
    return participantRepository.findById(participantId).map(ParticipantService::toInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ParticipantInfo> findByUser(UUID meetingId, UUID userId) {
    return participantRepository
        .findByMeetingAndUser(meetingId, userId)
        .map(ParticipantService::toInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipantInfo requireMember(UUID meetingId, UUID participantId) {
    return participantRepository
        .findById(participantId)
        .filter(participant -> participant.meetingId().equals(meetingId))
        .map(ParticipantService::toInfo)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not a participant of this meeting"));
  }

  @Override
  @Transactional(readOnly = true)
  public ParticipantInfo requireHost(UUID meetingId, UUID participantId) {
    ParticipantInfo participant = requireMember(meetingId, participantId);
    if (participant.role() != ParticipantRole.HOST) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can do this");
    }
    return participant;
  }

  private static ParticipantInfo toInfo(Participant participant) {
    return new ParticipantInfo(
        participant.id(),
        participant.meetingId(),
        participant.userId(),
        participant.displayName(),
        participant.role(),
        participant.mediaIdentity());
  }
}
