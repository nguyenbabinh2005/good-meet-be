package com.practice.goodmeet.modules.meeting.contract;

import java.util.UUID;

public interface MeetingSettings {

  void update(
      UUID meetingId, Boolean captionsEnabled, Boolean notesEnabled, Boolean assistantEnabled);
}
