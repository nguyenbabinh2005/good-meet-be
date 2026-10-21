package com.practice.goodmeet.modules.meeting.application;

import com.practice.goodmeet.modules.meeting.contract.MeetingSessionEndedEvent;
import com.practice.goodmeet.modules.meeting.domain.Meeting;
import com.practice.goodmeet.modules.meeting.domain.MeetingRepository;
import com.practice.goodmeet.modules.meeting.domain.MeetingSession;
import com.practice.goodmeet.modules.meeting.domain.ParticipantSession;
import com.practice.goodmeet.modules.meeting.domain.SessionRepository;
import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantInfo;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

  private static final String USER_PREFIX = "user_";
  private static final String GUEST_PREFIX = "guest_";

  private final MeetingRepository meetingRepository;
  private final SessionRepository sessionRepository;
  private final ParticipantDirectory participantDirectory;
  private final MediaRoom mediaRoom;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  SessionService(
      MeetingRepository meetingRepository,
      SessionRepository sessionRepository,
      ParticipantDirectory participantDirectory,
      MediaRoom mediaRoom,
      ApplicationEventPublisher eventPublisher,
      Clock clock) {
    this.meetingRepository = meetingRepository;
    this.sessionRepository = sessionRepository;
    this.participantDirectory = participantDirectory;
    this.mediaRoom = mediaRoom;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Transactional
  public void handleWebhook(MediaRoom.WebhookEvent event) {
    if (event.event() == null || event.roomId() == null) {
      return;
    }
    meetingRepository
        .findByMediaRoomId(event.roomId())
        .ifPresent(meeting -> dispatch(meeting, event));
  }

  @Transactional
  public void endActiveSession(Meeting meeting) {
    mediaRoom.deleteRoom(meeting.mediaRoomId());
    closeSession(meeting);
  }

  @Transactional(readOnly = true)
  public Optional<UUID> findActiveSessionId(UUID meetingId) {
    return sessionRepository.findActive(meetingId).map(MeetingSession::id);
  }

  private void dispatch(Meeting meeting, MediaRoom.WebhookEvent event) {
    switch (event.event()) {
      case "room_started" -> openSession(meeting);
      case "room_finished" -> closeSession(meeting);
      case "participant_joined" -> participantJoined(meeting, event.participantIdentity());
      case "participant_left" -> participantLeft(meeting, event.participantIdentity());
      default -> {}
    }
  }

  private MeetingSession openSession(Meeting meeting) {
    return sessionRepository
        .findActive(meeting.id())
        .orElseGet(
            () -> sessionRepository.save(MeetingSession.open(meeting.id(), clock.instant())));
  }

  private void closeSession(Meeting meeting) {
    sessionRepository
        .findActive(meeting.id())
        .ifPresent(
            session -> {
              sessionRepository
                  .findOpenParticipantSessions(session.id())
                  .forEach(open -> sessionRepository.save(open.close(clock.instant())));
              sessionRepository.save(session.end(clock.instant()));
              eventPublisher.publishEvent(new MeetingSessionEndedEvent(meeting.id(), session.id()));
            });
  }

  private void participantJoined(Meeting meeting, String identity) {
    Optional<ParticipantInfo> participant = findParticipant(meeting, identity);
    if (participant.isEmpty()) {
      return;
    }
    MeetingSession session = openSession(meeting);
    if (sessionRepository
        .findOpenParticipantSession(participant.get().id(), session.id())
        .isEmpty()) {
      sessionRepository.save(
          ParticipantSession.open(participant.get().id(), session.id(), clock.instant()));
    }
  }

  private void participantLeft(Meeting meeting, String identity) {
    Optional<ParticipantInfo> participant = findParticipant(meeting, identity);
    Optional<MeetingSession> session = sessionRepository.findActive(meeting.id());
    if (participant.isEmpty() || session.isEmpty()) {
      return;
    }
    sessionRepository
        .findOpenParticipantSession(participant.get().id(), session.get().id())
        .ifPresent(open -> sessionRepository.save(open.close(clock.instant())));
  }

  private Optional<ParticipantInfo> findParticipant(Meeting meeting, String identity) {
    if (identity == null) {
      return Optional.empty();
    }
    try {
      if (identity.startsWith(USER_PREFIX)) {
        UUID userId = UUID.fromString(identity.substring(USER_PREFIX.length()));
        return participantDirectory.findByUser(meeting.id(), userId);
      }
      if (identity.startsWith(GUEST_PREFIX)) {
        UUID participantId = UUID.fromString(identity.substring(GUEST_PREFIX.length()));
        return participantDirectory
            .findById(participantId)
            .filter(participant -> participant.meetingId().equals(meeting.id()));
      }
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
