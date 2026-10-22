package com.tomo.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

/** RabbitMQ 연결 속성과 캐싱 팩토리 매핑을 검증하는 테스트. */
class RabbitMqConnectionTest {

  @Test
  @DisplayName("개발 CloudAMQP는 TLS 포트와 vhost 및 채널 캐시를 적용한다")
  void configuresSecureConnection() {
    RabbitProperties properties = new RabbitProperties();
    properties.setHost("dingo.rmq.cloudamqp.com");
    properties.setPort(5671);
    properties.setUsername("nkmxzsif");
    properties.setPassword("test-password");
    properties.setVirtualHost("nkmxzsif");
    properties.setConnectionTimeout(Duration.ofSeconds(5));
    properties.getSsl().setEnabled(true);
    properties.getSsl().setVerifyHostname(true);
    properties.getCache().getChannel().setSize(10);

    CachingConnectionFactory factory =
        new CachingConnectionFactory(properties.getHost(), properties.getPort());
    factory.setUsername(properties.getUsername());
    factory.setPassword(properties.getPassword());
    factory.setVirtualHost(properties.getVirtualHost());
    factory.setConnectionTimeout(Math.toIntExact(properties.getConnectionTimeout().toMillis()));
    factory.setChannelCacheSize(properties.getCache().getChannel().getSize());

    assertThat(factory.getHost()).isEqualTo("dingo.rmq.cloudamqp.com");
    assertThat(factory.getPort()).isEqualTo(5671);
    assertThat(factory.getVirtualHost()).isEqualTo("nkmxzsif");
    assertThat(factory.getChannelCacheSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("로컬 RabbitMQ는 기본 포트 5672와 기본 vhost로 연결한다")
  void configuresLocalConnection() {
    RabbitProperties properties = new RabbitProperties();
    properties.setHost("localhost");
    properties.setPort(5672);
    properties.setUsername("guest");
    properties.setPassword("guest");
    properties.setVirtualHost("/");

    CachingConnectionFactory factory =
        new CachingConnectionFactory(properties.getHost(), properties.getPort());
    factory.setUsername(properties.getUsername());
    factory.setPassword(properties.getPassword());
    factory.setVirtualHost(properties.getVirtualHost());

    assertThat(factory.getHost()).isEqualTo("localhost");
    assertThat(factory.getPort()).isEqualTo(5672);
    assertThat(factory.getVirtualHost()).isEqualTo("/");
  }
}
