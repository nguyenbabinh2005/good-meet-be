package com.practice.goodmeet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

  private final ApplicationModules modules = ApplicationModules.of(GoodMeetApplication.class);

  @Test
  void verifiesModuleBoundaries() {
    assertThatCode(modules::verify).doesNotThrowAnyException();
  }

  @Test
  void detectsAllBusinessModules() {
    assertThat(modules.getModuleByName("modules.identity")).isPresent();
    assertThat(modules.getModuleByName("modules.participant")).isPresent();
    assertThat(modules.getModuleByName("modules.meeting")).isPresent();
    assertThat(modules.getModuleByName("modules.chat")).isPresent();
    assertThat(modules.getModuleByName("modules.presence")).isPresent();
    assertThat(modules.getModuleByName("modules.breakout")).isPresent();
  }
}
