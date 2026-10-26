package com.tomo.common.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** 루프백 관리 포트에서 제공하는 개발 서버의 상태 조회와 메트릭 수집 보안을 설정한다. */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class DevActuatorSecurityConfig {

  private static final String METRICS_ROLE = "METRICS";

  /**
   * 개발 배포의 헬스 체크와 Prometheus 메트릭 수집에 필요한 요청만 허용한다.
   *
   * @param http HTTP 보안 설정
   * @param username 메트릭 수집 전용 계정명
   * @param password 메트릭 수집 전용 비밀번호
   * @return 개발 서버 관리 엔드포인트 보안 필터 체인
   * @throws Exception 필터 체인 구성에 실패한 경우
   */
  @Bean
  @Order(1)
  public SecurityFilterChain devActuatorSecurityFilterChain(
      HttpSecurity http,
      @Value("${tomo.metrics.username:tomo-metrics}") String username,
      @Value("${tomo.metrics.password}") String password)
      throws Exception {
    UserDetailsService userDetailsService =
        new InMemoryUserDetailsManager(
            User.withUsername(username).password("{noop}" + password).roles(METRICS_ROLE).build());

    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(
        PasswordEncoderFactories.createDelegatingPasswordEncoder());

    return http.securityMatcher("/actuator", "/actuator/**")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/prometheus")
                    .hasRole(METRICS_ROLE)
                    .anyRequest()
                    .denyAll())
        .build();
  }
}
