package com.tomo.rabbitmq;

import java.util.UUID;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/** 개발 CloudAMQP의 TLS 연결과 메시지 송수신을 수동 검증하는 실행 도구. */
@TestConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration(RabbitAutoConfiguration.class)
public class DevRabbitMqVerification {

  /**
   * RabbitMQ 관련 설정만 로드해 임시 큐 송수신을 검증한 뒤 연결을 종료한다.
   *
   * @param args Spring 실행 인자
   */
  public static void main(String[] args) {
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(DevRabbitMqVerification.class)
            .profiles("dev")
            .web(WebApplicationType.NONE)
            .run(args)) {
      CachingConnectionFactory factory = context.getBean(CachingConnectionFactory.class);
      if (!factory.getRabbitConnectionFactory().isSSL()) {
        throw new IllegalStateException("RabbitMQ TLS가 비활성 상태입니다.");
      }

      try (Connection connection = factory.createConnection()) {
        System.out.println(
            "RabbitMQ TLS 연결 확인: "
                + factory.getHost()
                + ":"
                + factory.getPort()
                + "/"
                + factory.getVirtualHost());
      }

      RabbitTemplate rabbit = context.getBean(RabbitTemplate.class);
      AmqpAdmin admin = context.getBean(AmqpAdmin.class);
      String queueName = "tomo:verify:" + UUID.randomUUID();
      String payload = "tomo-verify-" + UUID.randomUUID();

      try {
        Queue queue = new Queue(queueName, false, false, true);
        admin.declareQueue(queue);
        rabbit.convertAndSend(queueName, payload);

        Object received = rabbit.receiveAndConvert(queueName, 5000);
        if (received == null) {
          throw new IllegalStateException("RabbitMQ 메시지 수신 타임아웃");
        }
        if (!payload.equals(received.toString())) {
          throw new IllegalStateException("수신 메시지 불일치: 기대값=" + payload + ", 실제값=" + received);
        }
        System.out.println("RabbitMQ 임시 큐 송수신 검증 확인");
      } finally {
        admin.deleteQueue(queueName);
      }
      System.out.println("CloudAMQP 검증 완료");
    }
  }
}
