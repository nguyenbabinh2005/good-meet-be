package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import com.practice.goodmeet.modules.meeting.domain.MeetingSession;
import com.practice.goodmeet.modules.meeting.domain.ParticipantSession;
import com.practice.goodmeet.modules.meeting.domain.SessionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaSessionRepository implements SessionRepository {

  private final SpringDataSessionRepository sessions;
  private final SpringDataParticipantSessionRepository participantSessions;

  JpaSessionRepository(
      SpringDataSessionRepository sessions,
      SpringDataParticipantSessionRepository participantSessions) {
    this.sessions = sessions;
    this.participantSessions = participantSessions;
  }

  @Override
  public MeetingSession save(MeetingSession session) {
    return toDomain(
        sessions.save(
            new SessionEntity(
                session.id(),
                session.meetingId(),
                session.status(),
                session.startedAt(),
                session.endedAt())));
  }

  @Override
  public Optional<MeetingSession> findActive(UUID meetingId) {
    return sessions
        .findByMeetingIdAndStatus(meetingId, MeetingSession.Status.ACTIVE)
        .map(JpaSessionRepository::toDomain);
  }

  @Override
  public ParticipantSession save(ParticipantSession participantSession) {
    return toDomain(
        participantSessions.save(
            new ParticipantSessionEntity(
                participantSession.id(),
                participantSession.participantId(),
                participantSession.sessionId(),
                participantSession.joinedAt(),
                participantSession.leftAt())));
  }

  @Override
  public Optional<ParticipantSession> findOpenParticipantSession(
      UUID participantId, UUID sessionId) {
    return participantSessions
        .findByParticipantIdAndSessionIdAndLeftAtIsNull(participantId, sessionId)
        .map(JpaSessionRepository::toDomain);
  }

  @Override
  public List<ParticipantSession> findOpenParticipantSessions(UUID sessionId) {
    return participantSessions.findBySessionIdAndLeftAtIsNull(sessionId).stream()
        .map(JpaSessionRepository::toDomain)
        .toList();
  }

  private static MeetingSession toDomain(SessionEntity entity) {
    return new MeetingSession(
        entity.getId(),
        entity.getMeetingId(),
        entity.getStatus(),
        entity.getStartedAt(),
        entity.getEndedAt());
  }

  private static ParticipantSession toDomain(ParticipantSessionEntity entity) {
    return new ParticipantSession(
        entity.getId(),
        entity.getParticipantId(),
        entity.getSessionId(),
        entity.getJoinedAt(),
        entity.getLeftAt());
  }
}
