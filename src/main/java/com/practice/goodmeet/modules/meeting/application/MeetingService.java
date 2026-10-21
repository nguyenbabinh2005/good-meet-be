package com.practice.goodmeet.modules.meeting.application;

import com.practice.goodmeet.modules.meeting.contract.MeetingInfo;
import com.practice.goodmeet.modules.meeting.contract.MeetingLookup;
import com.practice.goodmeet.modules.meeting.contract.MeetingSettings;
import com.practice.goodmeet.modules.meeting.contract.MeetingStatus;
import com.practice.goodmeet.modules.meeting.domain.Invitation;
import com.practice.goodmeet.modules.meeting.domain.Meeting;
import com.practice.goodmeet.modules.meeting.domain.MeetingRepository;
import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MeetingService implements MeetingLookup, MeetingSettings {

  private static final String CODE_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";

  public record InvitationView(UUID id, String email, Instant createdAt) {}

  private final MeetingRepository meetingRepository;
  private final ParticipantDirectory participantDirectory;
  private final SessionService sessionService;
  private final Clock clock;

  MeetingService(
      MeetingRepository meetingRepository,
      ParticipantDirectory participantDirectory,
      SessionService sessionService,
      Clock clock) {
    this.meetingRepository = meetingRepository;
    this.participantDirectory = participantDirectory;
    this.sessionService = sessionService;
    this.clock = clock;
  }

  @Transactional
  public MeetingInfo create(UUID userId, String displayName, String title) {
    Meeting meeting =
        meetingRepository.save(Meeting.instant(newPublicCode(), requireTitle(title), clock.instant()));
    participantDirectory.join(meeting.id(), userId, displayName, ParticipantRole.HOST);
    return toInfo(meeting);
  }

  @Transactional
  public MeetingInfo schedule(
      UUID userId, String displayName, String title, Instant start, Instant end) {
    validateSchedule(start, end);
    Meeting meeting =
        meetingRepository.save(
            Meeting.scheduled(newPublicCode(), requireTitle(title), start, end, clock.instant()));
    participantDirectory.join(meeting.id(), userId, displayName, ParticipantRole.HOST);
    return toInfo(meeting);
  }

  @Transactional
  public MeetingInfo reschedule(String code, UUID participantId, Instant start, Instant end) {
    Meeting meeting = requireHostMeeting(code, participantId);
    requireStatus(meeting, MeetingStatus.SCHEDULED, "Only a scheduled meeting can be rescheduled");
    validateSchedule(start, end);
    return toInfo(meetingRepository.save(meeting.rescheduled(start, end)));
  }

  @Transactional
  public MeetingInfo cancel(String code, UUID participantId) {
    Meeting meeting = requireHostMeeting(code, participantId);
    requireStatus(meeting, MeetingStatus.SCHEDULED, "Only a scheduled meeting can be cancelled");
    return toInfo(meetingRepository.save(meeting.cancelled()));
  }

  @Transactional
  public InvitationView invite(String code, UUID participantId, String email) {
    Meeting meeting = requireHostMeeting(code, participantId);
    if (meeting.isClosed()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting is already closed");
    }
    String normalized = Invitation.normalizeEmail(email);
    Invitation invitation =
        meetingRepository
            .findInvitationByEmail(meeting.id(), normalized)
            .orElseGet(
                () ->
                    meetingRepository.saveInvitation(
                        Invitation.of(meeting.id(), normalized, clock.instant())));
    return toView(invitation);
  }

  @Transactional(readOnly = true)
  public List<InvitationView> invitations(String code, UUID participantId) {
    Meeting meeting = requireHostMeeting(code, participantId);
    return meetingRepository.findInvitations(meeting.id()).stream()
        .map(MeetingService::toView)
        .toList();
  }

  @Transactional
  public void removeInvitation(String code, UUID participantId, UUID invitationId) {
    Meeting meeting = requireHostMeeting(code, participantId);
    Invitation invitation =
        meetingRepository
            .findInvitation(meeting.id(), invitationId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
    meetingRepository.deleteInvitation(invitation.id());
  }

  @Transactional
  public void endSession(String code, UUID participantId) {
    sessionService.endActiveSession(requireHostMeeting(code, participantId));
  }

  @Transactional
  public MeetingInfo end(String code, UUID participantId) {
    Meeting meeting = requireHostMeeting(code, participantId);
    if (meeting.status() == MeetingStatus.ENDED) {
      return toInfo(meeting);
    }
    if (meeting.status() == MeetingStatus.CANCELLED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting was cancelled");
    }
    sessionService.endActiveSession(meeting);
    return toInfo(meetingRepository.save(meeting.ended()));
  }

  @Transactional(readOnly = true)
  public Meeting requireHostMeeting(String code, UUID participantId) {
    Meeting meeting =
        meetingRepository
            .findByPublicCode(normalizeCode(code))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
    participantDirectory.requireHost(meeting.id(), participantId);
    return meeting;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MeetingInfo> findById(UUID meetingId) {
    return meetingRepository.findById(meetingId).map(MeetingService::toInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MeetingInfo> findByPublicCode(String publicCode) {
    return meetingRepository.findByPublicCode(normalizeCode(publicCode)).map(MeetingService::toInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UUID> findActiveSessionId(UUID meetingId) {
    return sessionService.findActiveSessionId(meetingId);
  }

  @Override
  @Transactional
  public void update(
      UUID meetingId, Boolean captionsEnabled, Boolean notesEnabled, Boolean assistantEnabled) {
    Meeting meeting =
        meetingRepository
            .findById(meetingId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
    meetingRepository.save(meeting.withSettings(captionsEnabled, notesEnabled, assistantEnabled));
  }

  static String normalizeCode(String code) {
    return code == null ? "" : code.strip().toLowerCase(Locale.ROOT);
  }

  private static MeetingInfo toInfo(Meeting meeting) {
    return new MeetingInfo(
        meeting.id(),
        meeting.publicCode(),
        meeting.title(),
        meeting.status(),
        meeting.scheduledStartAt(),
        meeting.scheduledEndAt(),
        meeting.mediaRoomId(),
        meeting.captionsEnabled(),
        meeting.notesEnabled(),
        meeting.assistantEnabled());
  }

  private static InvitationView toView(Invitation invitation) {
    return new InvitationView(invitation.id(), invitation.email(), invitation.createdAt());
  }

  private static void requireStatus(Meeting meeting, MeetingStatus expected, String message) {
    if (meeting.status() != expected) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
  }

  private void validateSchedule(Instant start, Instant end) {
    if (start == null || end == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end time are required");
    }
    if (!end.isAfter(start)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
    }
    if (!start.isAfter(clock.instant())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be in the future");
    }
  }

  private static String requireTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
    }
    return title;
  }

  private static String newPublicCode() {
    return "gm-" + randomChunk() + "-" + randomChunk();
  }

  private static String randomChunk() {
    StringBuilder chunk = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      chunk.append(CODE_ALPHABET.charAt(ThreadLocalRandom.current().nextInt(CODE_ALPHABET.length())));
    }
    return chunk.toString();
  }
}
