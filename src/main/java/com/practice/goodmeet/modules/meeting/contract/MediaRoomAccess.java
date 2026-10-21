package com.practice.goodmeet.modules.meeting.contract;

public interface MediaRoomAccess {

  MediaToken issueToken(String roomId, String identity, String displayName, boolean canModerate);

  void deleteRoom(String roomId);
}
