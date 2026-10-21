package com.practice.goodmeet.modules.meeting.api;

import com.practice.goodmeet.modules.meeting.application.MediaRoom;
import com.practice.goodmeet.modules.meeting.application.MeetingService;
import com.practice.goodmeet.modules.meeting.application.RoomAdminService;
import com.practice.goodmeet.modules.meeting.contract.MeetingInfo;
import com.practice.goodmeet.shared.ApiHeaders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
class MeetingController {

  record CreateRequest(
      @NotNull UUID userId,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(max = 200) String title) {}

  record ScheduleRequest(
      @NotNull UUID userId,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(max = 200) String title,
      @NotNull Instant scheduledStartAt,
      @NotNull Instant scheduledEndAt) {}

  record RescheduleRequest(@NotNull Instant scheduledStartAt, @NotNull Instant scheduledEndAt) {}

  record InviteRequest(@NotBlank @Email @Size(max = 255) String email) {}

  record MuteRequest(@NotBlank String trackSid) {}

  private final MeetingService meetingService;
  private final RoomAdminService roomAdminService;

  MeetingController(MeetingService meetingService, RoomAdminService roomAdminService) {
    this.meetingService = meetingService;
    this.roomAdminService = roomAdminService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  MeetingInfo create(@Valid @RequestBody CreateRequest request) {
    return meetingService.create(request.userId(), request.displayName(), request.title());
  }

  @PostMapping("/scheduled")
  @ResponseStatus(HttpStatus.CREATED)
  MeetingInfo schedule(@Valid @RequestBody ScheduleRequest request) {
    return meetingService.schedule(
        request.userId(),
        request.displayName(),
        request.title(),
        request.scheduledStartAt(),
        request.scheduledEndAt());
  }

  @PutMapping("/{code}/schedule")
  MeetingInfo reschedule(
      @PathVariable String code,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId,
      @Valid @RequestBody RescheduleRequest request) {
    return meetingService.reschedule(
        code, participantId, request.scheduledStartAt(), request.scheduledEndAt());
  }

  @PostMapping("/{code}/cancel")
  MeetingInfo cancel(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    return meetingService.cancel(code, participantId);
  }

  @PostMapping("/{code}/end-session")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void endSession(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    meetingService.endSession(code, participantId);
  }

  @PostMapping("/{code}/end")
  MeetingInfo end(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    return meetingService.end(code, participantId);
  }

  @PostMapping("/{code}/invitations")
  @ResponseStatus(HttpStatus.CREATED)
  MeetingService.InvitationView invite(
      @PathVariable String code,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId,
      @Valid @RequestBody InviteRequest request) {
    return meetingService.invite(code, participantId, request.email());
  }

  @GetMapping("/{code}/invitations")
  List<MeetingService.InvitationView> invitations(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    return meetingService.invitations(code, participantId);
  }

  @DeleteMapping("/{code}/invitations/{invitationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void removeInvitation(
      @PathVariable String code,
      @PathVariable UUID invitationId,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    meetingService.removeInvitation(code, participantId, invitationId);
  }

  @GetMapping("/{code}/room-participants")
  List<MediaRoom.RoomParticipant> roomParticipants(
      @PathVariable String code, @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    return roomAdminService.listParticipants(code, participantId);
  }

  @PostMapping("/{code}/room-participants/{identity}/kick")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void kick(
      @PathVariable String code,
      @PathVariable String identity,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    roomAdminService.kick(code, participantId, identity);
  }

  @PostMapping("/{code}/room-participants/{identity}/mute")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void mute(
      @PathVariable String code,
      @PathVariable String identity,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId,
      @Valid @RequestBody MuteRequest request) {
    roomAdminService.mute(code, participantId, identity, request.trackSid());
  }
}
