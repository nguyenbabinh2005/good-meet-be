package com.practice.goodmeet.modules.meeting.application;

import com.practice.goodmeet.modules.meeting.contract.MediaRoomAccess;
import java.util.List;

public interface MediaRoom extends MediaRoomAccess {

  List<RoomParticipant> listParticipants(String roomId);

  void removeParticipant(String roomId, String identity);

  void muteTrack(String roomId, String identity, String trackSid);

  WebhookEvent decodeWebhook(String body, String authorization);

  record RoomParticipant(String identity, String name, List<Track> tracks) {}

  record Track(String sid, String type, boolean muted) {}

  record WebhookEvent(String event, String roomId, String participantIdentity) {}
}
