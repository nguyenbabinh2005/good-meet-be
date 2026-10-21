package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import com.practice.goodmeet.modules.meeting.domain.Invitation;
import com.practice.goodmeet.modules.meeting.domain.Meeting;
import com.practice.goodmeet.modules.meeting.domain.MeetingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaMeetingRepository implements MeetingRepository {

  private final SpringDataMeetingRepository meetings;
  private final SpringDataInvitationRepository invitations;

  JpaMeetingRepository(
      SpringDataMeetingRepository meetings, SpringDataInvitationRepository invitations) {
    this.meetings = meetings;
    this.invitations = invitations;
  }

  @Override
  public Meeting save(Meeting meeting) {
    return toDomain(meetings.save(toEntity(meeting)));
  }

  @Override
  public Optional<Meeting> findById(UUID id) {
    return meetings.findById(id).map(JpaMeetingRepository::toDomain);
  }

  @Override
  public Optional<Meeting> findByPublicCode(String publicCode) {
    return meetings.findByPublicCode(publicCode).map(JpaMeetingRepository::toDomain);
  }

  @Override
  public Optional<Meeting> findByMediaRoomId(String mediaRoomId) {
    return meetings.findByMediaRoomId(mediaRoomId).map(JpaMeetingRepository::toDomain);
  }

  @Override
  public Invitation saveInvitation(Invitation invitation) {
    return toDomain(
        invitations.save(
            new InvitationEntity(
                invitation.id(),
                invitation.meetingId(),
                invitation.email(),
                invitation.createdAt())));
  }

  @Override
  public List<Invitation> findInvitations(UUID meetingId) {
    return invitations.findByMeetingIdOrderByCreatedAtAsc(meetingId).stream()
        .map(JpaMeetingRepository::toDomain)
        .toList();
  }

  @Override
  public Optional<Invitation> findInvitation(UUID meetingId, UUID invitationId) {
    return invitations.findByIdAndMeetingId(invitationId, meetingId).map(JpaMeetingRepository::toDomain);
  }

  @Override
  public Optional<Invitation> findInvitationByEmail(UUID meetingId, String email) {
    return invitations.findByMeetingIdAndEmail(meetingId, email).map(JpaMeetingRepository::toDomain);
  }

  @Override
  public void deleteInvitation(UUID invitationId) {
    invitations.deleteById(invitationId);
  }

  private static Meeting toDomain(MeetingEntity entity) {
    return new Meeting(
        entity.getId(),
        entity.getPublicCode(),
        entity.getTitle(),
        entity.getStatus(),
        entity.getScheduledStartAt(),
        entity.getScheduledEndAt(),
        entity.getMediaRoomId(),
        entity.isCaptionsEnabled(),
        entity.isNotesEnabled(),
        entity.isAssistantEnabled(),
        entity.getCreatedAt());
  }

  private static MeetingEntity toEntity(Meeting meeting) {
    return new MeetingEntity(
        meeting.id(),
        meeting.publicCode(),
        meeting.title(),
        meeting.status(),
        meeting.scheduledStartAt(),
        meeting.scheduledEndAt(),
        meeting.mediaRoomId(),
        meeting.captionsEnabled(),
        meeting.notesEnabled(),
        meeting.assistantEnabled(),
        meeting.createdAt());
  }

  private static Invitation toDomain(InvitationEntity entity) {
    return new Invitation(
        entity.getId(), entity.getMeetingId(), entity.getEmail(), entity.getCreatedAt());
  }
}
