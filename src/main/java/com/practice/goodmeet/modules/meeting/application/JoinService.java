package com.practice.goodmeet.modules.meeting.application;

import com.practice.goodmeet.modules.meeting.contract.MediaToken;
import com.practice.goodmeet.modules.meeting.contract.MeetingStatus;
import com.practice.goodmeet.modules.meeting.domain.JoinRequest;
import com.practice.goodmeet.modules.meeting.domain.JoinRequestStore;
import com.practice.goodmeet.modules.meeting.domain.Meeting;
import com.practice.goodmeet.modules.meeting.domain.MeetingRepository;
import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantInfo;
import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JoinService {

  public record JoinResult(
      UUID meetingId,
      String publicCode,
      UUID participantId,
      String displayName,
      ParticipantRole role,
      String mediaUrl,
      String mediaToken,
      Instant mediaTokenExpiresAt) {}

  public record JoinOutcome(JoinResult joined, UUID requestId) {}

  private final MeetingRepository meetingRepository;
  private final ParticipantDirectory participantDirectory;
  private final MediaRoom mediaRoom;
  private final JoinRequestStore requestStore;
  private final MeetingNotifier notifier;
  private final Clock clock;

  JoinService(
      MeetingRepository meetingRepository,
      ParticipantDirectory participantDirectory,
      MediaRoom mediaRoom,
      JoinRequestStore requestStore,
      MeetingNotifier notifier,
      Clock clock) {
    this.meetingRepository = meetingRepository;
    this.participantDirectory = participantDirectory;
    this.mediaRoom = mediaRoom;
    this.requestStore = requestStore;
    this.notifier = notifier;
    this.clock = clock;
  }

  @Transactional
  public JoinOutcome join(String code, UUID userId, String displayName) {
    Meeting meeting = findOpenMeeting(code);
    Optional<ParticipantInfo> host =
        userId == null
            ? Optional.empty()
            : participantDirectory
                .findByUser(meeting.id(), userId)
                .filter(participant -> participant.role() == ParticipantRole.HOST);
    if (host.isPresent()) {
      return new JoinOutcome(admitHost(meeting, host.get()), null);
    }
    if (meeting.status() != MeetingStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Meeting has not started");
    }
    if (displayName == null || displayName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name is required");
    }
    JoinRequest request =
        requestStore.save(JoinRequest.of(meeting.id(), userId, displayName.strip(), clock.instant()));
    notifier.waitingRoomChanged(
        meeting.id(),
        new MeetingNotifier.WaitingRoomEvent(
            "REQUESTED", request.id(), request.displayName(), request.requestedAt()));
    return new JoinOutcome(null, request.id());
  }

  @Transactional
  public void decide(UUID requestId, UUID hostParticipantId, boolean approved) {
    JoinRequest request =
        requestStore
            .findById(requestId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"));
    participantDirectory.requireHost(request.meetingId(), hostParticipantId);
    Meeting meeting =
        meetingRepository
            .findById(request.meetingId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
    if (approved) {
      requireNotClosed(meeting);
      ParticipantInfo participant =
          participantDirectory.join(
              meeting.id(),
              request.userId(),
              request.displayName(),
              request.userId() == null ? ParticipantRole.GUEST : ParticipantRole.PARTICIPANT);
      notifier.joinRequestDecided(
          requestId, new MeetingNotifier.JoinDecision(true, toResult(meeting, participant)));
    } else {
      notifier.joinRequestDecided(requestId, new MeetingNotifier.JoinDecision(false, null));
    }
    requestStore.deleteById(requestId);
    notifier.waitingRoomChanged(
        meeting.id(),
        new MeetingNotifier.WaitingRoomEvent(
            "RESOLVED", request.id(), request.displayName(), request.requestedAt()));
  }

  private JoinResult admitHost(Meeting meeting, ParticipantInfo host) {
    Meeting current =
        meeting.status() == MeetingStatus.SCHEDULED
            ? meetingRepository.save(meeting.started())
            : meeting;
    return toResult(current, host);
  }

  private JoinResult toResult(Meeting meeting, ParticipantInfo participant) {
    MediaToken token =
        mediaRoom.issueToken(
            meeting.mediaRoomId(),
            participant.mediaIdentity(),
            participant.displayName(),
            participant.role() == ParticipantRole.HOST);
    return new JoinResult(
        meeting.id(),
        meeting.publicCode(),
        participant.id(),
        participant.displayName(),
        participant.role(),
        token.url(),
        token.token(),
        token.expiresAt());
  }

  private Meeting findOpenMeeting(String code) {
    Meeting meeting =
        meetingRepository
            .findByPublicCode(MeetingService.normalizeCode(code))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
    requireNotClosed(meeting);
    return meeting;
  }

  private static void requireNotClosed(Meeting meeting) {
    if (meeting.status() == MeetingStatus.ENDED) {
      throw new ResponseStatusException(HttpStatus.GONE, "Meeting has ended");
    }
    if (meeting.status() == MeetingStatus.CANCELLED) {
      throw new ResponseStatusException(HttpStatus.GONE, "Meeting was cancelled");
    }
  }
}
