package com.tomo.common.auth.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** 루프백 관리 포트에서 제공하는 개발 서버의 상태 조회를 허용한다. */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class DevHealthSecurityConfig {

  /**
   * 개발 배포의 헬스 체크에 필요한 GET 요청만 허용한다.
   *
   * @param http HTTP 보안 설정
   * @return 개발 서버 상태 조회 필터 체인
   * @throws Exception 필터 체인 구성에 실패한 경우
   */
  @Bean
  @Order(1)
  public SecurityFilterChain devHealthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher(EndpointRequest.to(HealthEndpoint.class))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .build();
  }
}
