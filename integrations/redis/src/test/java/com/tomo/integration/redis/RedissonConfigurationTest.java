package com.tomo.integration.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.config.Config;
import org.redisson.config.SslVerificationMode;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

class RedissonConfigurationTest {

  @Test
  @DisplayName("개발 Valkey는 TLS와 호스트 검증 및 제한된 연결 수를 사용한다")
  void configuresSecureConnection() {
    // given
    RedisProperties redis = new RedisProperties();
    redis.setHost("valkey.example.test");
    redis.setPort(17304);
    redis.setUsername("default");
    redis.setPassword("test-password");
    redis.setConnectTimeout(Duration.ofSeconds(3));
    redis.setTimeout(Duration.ofSeconds(3));
    redis.getSsl().setEnabled(true);

    // when
    Config config = new RedissonConfiguration().redissonConfig(redis, 4, 1, 2, 1);

    // then
    assertThat(config.useSingleServer().getAddress())
        .isEqualTo("rediss://valkey.example.test:17304");
    assertThat(config.useSingleServer().getSslVerificationMode())
        .isEqualTo(SslVerificationMode.STRICT);
    assertThat(config.useSingleServer().getConnectionPoolSize()).isEqualTo(4);
    assertThat(config.useSingleServer().getSubscriptionConnectionPoolSize()).isEqualTo(2);
  }

  @Test
  @DisplayName("로컬 Redis는 TLS 없이 빈 인증 정보를 제외하고 연결한다")
  void configuresLocalConnection() {
    // given
    RedisProperties redis = new RedisProperties();
    redis.setUsername("");
    redis.setPassword("");
    redis.setConnectTimeout(Duration.ofSeconds(3));
    redis.setTimeout(Duration.ofSeconds(3));

    // when
    Config config = new RedissonConfiguration().redissonConfig(redis, 4, 1, 2, 1);

    // then
    assertThat(config.useSingleServer().getAddress()).isEqualTo("redis://localhost:6379");
    assertThat(config.useSingleServer().getUsername()).isNull();
    assertThat(config.useSingleServer().getPassword()).isNull();
  }
}
