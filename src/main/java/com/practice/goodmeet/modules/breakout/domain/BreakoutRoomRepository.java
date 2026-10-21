package com.practice.goodmeet.modules.breakout.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BreakoutRoomRepository {

  BreakoutRoom save(BreakoutRoom room);

  Optional<BreakoutRoom> findById(UUID id);

  List<BreakoutRoom> findOpenBySession(UUID sessionId);
}
