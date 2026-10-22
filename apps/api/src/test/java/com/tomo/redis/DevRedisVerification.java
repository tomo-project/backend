package com.tomo.redis;

import com.tomo.integration.redis.RedissonConfiguration;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 개발 Valkey의 TLS·데이터 만료·분산 락을 수동 검증하는 실행 도구. */
@TestConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration(RedisAutoConfiguration.class)
@Import(RedissonConfiguration.class)
public class DevRedisVerification {

  /**
   * Redis 관련 설정만 로드해 임시 키로 검증한 뒤 연결을 종료한다.
   *
   * @param args Spring 실행 인자
   * @throws InterruptedException 만료 또는 락 확인 중 실행이 중단된 경우
   */
  public static void main(String[] args) throws InterruptedException {
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(DevRedisVerification.class)
            .profiles("dev")
            .web(WebApplicationType.NONE)
            .run(args)) {
      LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);
      if (!factory.getClientConfiguration().isUseSsl()) {
        throw new IllegalStateException("Lettuce TLS가 비활성 상태입니다.");
      }
      if (!(factory.getClientConfiguration()
          instanceof LettucePoolingClientConfiguration pooling)) {
        throw new IllegalStateException("Lettuce 연결 풀이 비활성 상태입니다.");
      }
      System.out.println("Lettuce TLS 및 연결 풀 확인: 최대 " + pooling.getPoolConfig().getMaxTotal());
      StringRedisTemplate redis = context.getBean(StringRedisTemplate.class);
      String prefix = "tomo:verify:" + UUID.randomUUID();
      String dataKey = prefix + ":data";
      String expiryKey = prefix + ":expiry";
      try {
        redis.opsForValue().set(dataKey, "test-value", Duration.ofSeconds(30));
        if (!"test-value".equals(redis.opsForValue().get(dataKey))) {
          throw new IllegalStateException("Valkey 읽기·쓰기 검증 실패");
        }
        Long ttl = redis.getExpire(dataKey);
        if (ttl == null || ttl <= 0 || ttl > 30) {
          throw new IllegalStateException("Valkey TTL 설정 검증 실패");
        }
        if (!Boolean.TRUE.equals(redis.delete(dataKey))
            || Boolean.TRUE.equals(redis.hasKey(dataKey))) {
          throw new IllegalStateException("Valkey 삭제 검증 실패");
        }
        System.out.println("Lettuce 읽기·쓰기·삭제·TTL 설정 확인");
        redis.opsForValue().set(expiryKey, "expire-test", Duration.ofSeconds(1));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (Boolean.TRUE.equals(redis.hasKey(expiryKey)) && System.nanoTime() < deadline) {
          Thread.sleep(100);
        }
        if (redis.opsForValue().get(expiryKey) != null) {
          throw new IllegalStateException("Valkey 만료 검증 실패");
        }
        System.out.println("Lettuce TTL 만료 확인");
        verifyLock(context, prefix + ":lock");
      } finally {
        redis.delete(dataKey);
        redis.delete(expiryKey);
      }
      System.out.println("Valkey 검증 완료");
    }
  }

  private static void verifyLock(ConfigurableApplicationContext context, String key)
      throws InterruptedException {
    RedissonClient first = context.getBean(RedissonClient.class);
    RedissonClient second = Redisson.create(context.getBean(Config.class));
    try {
      RLock firstLock = first.getLock(key);
      RLock secondLock = second.getLock(key);
      if (!firstLock.tryLock(3, 30, TimeUnit.SECONDS)) {
        throw new IllegalStateException("첫 번째 클라이언트의 락 획득 실패");
      }
      try {
        if (secondLock.tryLock(1, 30, TimeUnit.SECONDS)) {
          secondLock.unlock();
          throw new IllegalStateException("다른 클라이언트가 점유한 락에 진입했습니다.");
        }
      } finally {
        if (firstLock.isHeldByCurrentThread()) {
          firstLock.unlock();
        }
      }
      if (!secondLock.tryLock(3, 30, TimeUnit.SECONDS)) {
        throw new IllegalStateException("해제 후 두 번째 클라이언트의 락 획득 실패");
      }
      try {
        System.out.println("Redisson 클라이언트 간 락 경합·해제·재획득 확인");
      } finally {
        if (secondLock.isHeldByCurrentThread()) {
          secondLock.unlock();
        }
      }
    } finally {
      second.shutdown();
    }
  }
}
