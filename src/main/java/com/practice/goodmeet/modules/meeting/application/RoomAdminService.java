package com.practice.goodmeet.modules.meeting.application;

import com.practice.goodmeet.modules.meeting.domain.Meeting;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RoomAdminService {

  private final MeetingService meetingService;
  private final MediaRoom mediaRoom;

  RoomAdminService(MeetingService meetingService, MediaRoom mediaRoom) {
    this.meetingService = meetingService;
    this.mediaRoom = mediaRoom;
  }

  public List<MediaRoom.RoomParticipant> listParticipants(String code, UUID participantId) {
    Meeting meeting = meetingService.requireHostMeeting(code, participantId);
    return mediaRoom.listParticipants(meeting.mediaRoomId());
  }

  public void kick(String code, UUID participantId, String identity) {
    Meeting meeting = meetingService.requireHostMeeting(code, participantId);
    mediaRoom.removeParticipant(meeting.mediaRoomId(), identity);
  }

  public void mute(String code, UUID participantId, String identity, String trackSid) {
    Meeting meeting = meetingService.requireHostMeeting(code, participantId);
    mediaRoom.muteTrack(meeting.mediaRoomId(), identity, trackSid);
  }
}
