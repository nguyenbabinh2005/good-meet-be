package com.practice.goodmeet.modules.breakout.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataBreakoutRoomRepository extends JpaRepository<BreakoutRoomEntity, UUID> {

  List<BreakoutRoomEntity> findBySessionIdAndClosedAtIsNull(UUID sessionId);
}
