package com.tomo.common.auth.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(DevActuatorSecurityConfigTest.TestActuatorController.class)
@Import({
  DevActuatorSecurityConfig.class,
  DevActuatorSecurityConfigTest.TestActuatorController.class
})
@ActiveProfiles("dev")
@TestPropertySource(
    properties = {"tomo.metrics.username=test-metrics", "tomo.metrics.password=test-secret"})
class DevActuatorSecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("헬스 체크 엔드포인트는 인증 없이 접근할 수 있다")
  void allowsHealthWithoutAuthentication() throws Exception {
    // when
    ResultActions result = mockMvc.perform(get("/actuator/health"));

    // then
    result.andExpect(status().isOk());
  }

  @Test
  @DisplayName("Prometheus 메트릭 엔드포인트는 인증 없이 접근하면 401을 반환한다")
  void requiresAuthenticationForPrometheus() throws Exception {
    // when
    ResultActions result = mockMvc.perform(get("/actuator/prometheus"));

    // then
    result.andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Prometheus 메트릭 엔드포인트는 잘못된 인증 정보로 접근하면 401을 반환한다")
  void rejectsInvalidCredentialsForPrometheus() throws Exception {
    // when
    ResultActions result =
        mockMvc.perform(
            get("/actuator/prometheus").with(httpBasic("test-metrics", "wrong-password")));

    // then
    result.andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Prometheus 메트릭 엔드포인트는 올바른 Basic Auth 인증 정보로 접근할 수 있다")
  void allowsPrometheusWithValidCredentials() throws Exception {
    // when
    ResultActions result =
        mockMvc.perform(get("/actuator/prometheus").with(httpBasic("test-metrics", "test-secret")));

    // then
    result.andExpect(status().isOk());
  }

  @Test
  @DisplayName("Prometheus 메트릭 엔드포인트에 GET 이외의 요청을 보내면 접근을 거부한다")
  void rejectsNonGetRequestsOnPrometheus() throws Exception {
    // when
    ResultActions result =
        mockMvc.perform(
            post("/actuator/prometheus").with(httpBasic("test-metrics", "test-secret")));

    // then
    result.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("허용되지 않은 관리 엔드포인트는 접근을 거부한다")
  void rejectsUnallowedActuatorEndpoints() throws Exception {
    // when
    ResultActions result =
        mockMvc.perform(get("/actuator/env").with(httpBasic("test-metrics", "test-secret")));

    // then
    result.andExpect(status().isForbidden());
  }

  @RestController
  static class TestActuatorController {

    @GetMapping("/actuator/health")
    String health() {
      return "{\"status\":\"UP\"}";
    }

    @GetMapping("/actuator/prometheus")
    String prometheus() {
      return "# HELP jvm_memory_used_bytes...";
    }

    @PostMapping("/actuator/prometheus")
    String postPrometheus() {
      return "denied";
    }

    @GetMapping("/actuator/env")
    String env() {
      return "env";
    }
  }
}
