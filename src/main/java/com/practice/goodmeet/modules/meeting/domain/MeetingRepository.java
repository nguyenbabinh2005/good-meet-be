package com.practice.goodmeet.modules.meeting.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository {

  Meeting save(Meeting meeting);

  Optional<Meeting> findById(UUID id);

  Optional<Meeting> findByPublicCode(String publicCode);

  Optional<Meeting> findByMediaRoomId(String mediaRoomId);

  Invitation saveInvitation(Invitation invitation);

  List<Invitation> findInvitations(UUID meetingId);

  Optional<Invitation> findInvitation(UUID meetingId, UUID invitationId);

  Optional<Invitation> findInvitationByEmail(UUID meetingId, String email);

  void deleteInvitation(UUID invitationId);
}
