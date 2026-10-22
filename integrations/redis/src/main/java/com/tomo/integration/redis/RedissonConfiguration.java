package com.tomo.integration.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SslVerificationMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/** 프로필별 Redis 접속 정보와 연결 수를 Redisson에 적용한다. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
    name = "tomo.redis.redisson.enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(RedisProperties.class)
public class RedissonConfiguration {

  /**
   * Lettuce와 같은 엔드포인트·인증 정보·TLS 여부로 연결 설정을 만든다.
   *
   * @param redis Spring Redis 접속 설정
   * @param poolSize 일반 연결 최대 수
   * @param idleSize 일반 연결 최소 수
   * @param subscriptionPoolSize 구독 연결 최대 수
   * @param subscriptionIdleSize 구독 연결 최소 수
   * @return 프로필의 접속 정보와 연결 제한을 적용한 Redisson 설정
   */
  @Bean
  public Config redissonConfig(
      RedisProperties redis,
      @Value("${tomo.redis.redisson.connection-pool-size}") int poolSize,
      @Value("${tomo.redis.redisson.connection-minimum-idle-size}") int idleSize,
      @Value("${tomo.redis.redisson.subscription-connection-pool-size}") int subscriptionPoolSize,
      @Value("${tomo.redis.redisson.subscription-connection-minimum-idle-size}")
          int subscriptionIdleSize) {
    String scheme = redis.getSsl().isEnabled() ? "rediss://" : "redis://";
    Config config = new Config();
    config.setThreads(2).setNettyThreads(2);
    config
        .useSingleServer()
        .setAddress(scheme + redis.getHost() + ":" + redis.getPort())
        .setUsername(StringUtils.hasText(redis.getUsername()) ? redis.getUsername() : null)
        .setPassword(StringUtils.hasText(redis.getPassword()) ? redis.getPassword() : null)
        .setDatabase(redis.getDatabase())
        .setConnectTimeout(Math.toIntExact(redis.getConnectTimeout().toMillis()))
        .setTimeout(Math.toIntExact(redis.getTimeout().toMillis()))
        .setRetryAttempts(1)
        .setConnectionPoolSize(poolSize)
        .setConnectionMinimumIdleSize(idleSize)
        .setSubscriptionConnectionPoolSize(subscriptionPoolSize)
        .setSubscriptionConnectionMinimumIdleSize(subscriptionIdleSize)
        .setSslVerificationMode(SslVerificationMode.STRICT);
    return config;
  }

  /**
   * 애플리케이션 종료 시 연결을 정리하는 Redisson 클라이언트를 만든다.
   *
   * @param config 접속 설정
   * @return Redisson 클라이언트
   */
  @Bean(destroyMethod = "shutdown")
  public RedissonClient redissonClient(Config config) {
    return Redisson.create(config);
  }
}
