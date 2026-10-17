package com.tomo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** TOMO API 서버의 실행 진입점입니다. */
@SpringBootApplication
public class TomoApiApplication {

  /**
   * API 서버를 실행합니다.
   *
   * @param args 실행 시 전달한 애플리케이션 인자
   */
  public static void main(String[] args) {
    SpringApplication.run(TomoApiApplication.class, args);
  }
}
