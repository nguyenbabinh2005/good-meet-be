package com.practice.goodmeet.modules.breakout.application;

import com.practice.goodmeet.modules.breakout.domain.BreakoutAssignmentStore;
import com.practice.goodmeet.modules.breakout.domain.BreakoutRoom;
import com.practice.goodmeet.modules.breakout.domain.BreakoutRoomRepository;
import com.practice.goodmeet.modules.meeting.contract.MediaRoomAccess;
import com.practice.goodmeet.modules.meeting.contract.MediaToken;
import com.practice.goodmeet.modules.meeting.contract.MeetingInfo;
import com.practice.goodmeet.modules.meeting.contract.MeetingLookup;
import com.practice.goodmeet.modules.meeting.contract.MeetingSessionEndedEvent;
import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantInfo;
import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BreakoutService {

  public record BreakoutRoomView(UUID id, UUID meetingId, String name, Instant createdAt) {}

  private final BreakoutRoomRepository roomRepository;
  private final BreakoutAssignmentStore assignmentStore;
  private final MeetingLookup meetingLookup;
  private final ParticipantDirectory participantDirectory;
  private final MediaRoomAccess mediaRoomAccess;
  private final BreakoutNotifier notifier;
  private final Clock clock;

  BreakoutService(
      BreakoutRoomRepository roomRepository,
      BreakoutAssignmentStore assignmentStore,
      MeetingLookup meetingLookup,
      ParticipantDirectory participantDirectory,
      MediaRoomAccess mediaRoomAccess,
      BreakoutNotifier notifier,
      Clock clock) {
    this.roomRepository = roomRepository;
    this.assignmentStore = assignmentStore;
    this.meetingLookup = meetingLookup;
    this.participantDirectory = participantDirectory;
    this.mediaRoomAccess = mediaRoomAccess;
    this.notifier = notifier;
    this.clock = clock;
  }

  @Transactional
  public BreakoutRoomView create(String code, UUID hostParticipantId, String name) {
    MeetingInfo meeting = findMeeting(code);
    participantDirectory.requireHost(meeting.id(), hostParticipantId);
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room name is required");
    }
    UUID sessionId =
        meetingLookup
            .findActiveSessionId(meeting.id())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT, "The meeting has no active session"));
    BreakoutRoom room =
        roomRepository.save(BreakoutRoom.create(meeting.id(), sessionId, name, clock.instant()));
    return new BreakoutRoomView(room.id(), room.meetingId(), room.name(), room.createdAt());
  }

  public void assign(String code, UUID hostParticipantId, UUID roomId, UUID participantId) {
    MeetingInfo meeting = findMeeting(code);
    participantDirectory.requireHost(meeting.id(), hostParticipantId);
    BreakoutRoom room = findOpenRoom(meeting, roomId);
    ParticipantInfo target = findTarget(meeting, participantId);
    assignmentStore.assign(target.id(), room.id());
    MediaToken token =
        mediaRoomAccess.issueToken(
            room.mediaRoomId(),
            target.mediaIdentity(),
            target.displayName(),
            target.role() == ParticipantRole.HOST);
    notifier.roomChanged(
        target.id(),
        new BreakoutNotifier.RoomChange(
            "BREAKOUT",
            room.id(),
            room.name(),
            token.url(),
            token.token(),
            token.expiresAt()));
  }

  public void returnToMain(String code, UUID callerParticipantId, UUID roomId, UUID participantId) {
    MeetingInfo meeting = findMeeting(code);
    ParticipantInfo caller = participantDirectory.requireMember(meeting.id(), callerParticipantId);
    if (!caller.id().equals(participantId) && caller.role() != ParticipantRole.HOST) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the host can move another participant");
    }
    BreakoutRoom room = findOpenRoom(meeting, roomId);
    ParticipantInfo target = findTarget(meeting, participantId);
    if (assignmentStore.findRoom(target.id()).filter(room.id()::equals).isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "The participant is not in this breakout room");
    }
    assignmentStore.unassign(target.id());
    notifyMainRoom(meeting, target);
  }

  @Transactional
  public void close(String code, UUID hostParticipantId, UUID roomId) {
    MeetingInfo meeting = findMeeting(code);
    participantDirectory.requireHost(meeting.id(), hostParticipantId);
    BreakoutRoom room = findOpenRoom(meeting, roomId);
    for (UUID participantId : assignmentStore.findParticipants(room.id())) {
      participantDirectory
          .findById(participantId)
          .ifPresent(participant -> notifyMainRoom(meeting, participant));
    }
    closeRoom(room);
  }

  @EventListener
  public void onSessionEnded(MeetingSessionEndedEvent event) {
    roomRepository.findOpenBySession(event.sessionId()).forEach(this::closeRoom);
  }

  private void closeRoom(BreakoutRoom room) {
    assignmentStore.clearRoom(room.id());
    mediaRoomAccess.deleteRoom(room.mediaRoomId());
    roomRepository.save(room.closed(clock.instant()));
  }

  private void notifyMainRoom(MeetingInfo meeting, ParticipantInfo participant) {
    MediaToken token =
        mediaRoomAccess.issueToken(
            meeting.mediaRoomId(),
            participant.mediaIdentity(),
            participant.displayName(),
            participant.role() == ParticipantRole.HOST);
    notifier.roomChanged(
        participant.id(),
        new BreakoutNotifier.RoomChange(
            "MAIN", null, null, token.url(), token.token(), token.expiresAt()));
  }

  private MeetingInfo findMeeting(String code) {
    return meetingLookup
        .findByPublicCode(code)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
  }

  private BreakoutRoom findOpenRoom(MeetingInfo meeting, UUID roomId) {
    BreakoutRoom room =
        roomRepository
            .findById(roomId)
            .filter(found -> found.meetingId().equals(meeting.id()))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Breakout room not found"));
    if (!room.isOpen()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Breakout room is closed");
    }
    return room;
  }

  private ParticipantInfo findTarget(MeetingInfo meeting, UUID participantId) {
    return participantDirectory
        .findById(participantId)
        .filter(participant -> participant.meetingId().equals(meeting.id()))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
  }
}
