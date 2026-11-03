package com.tomo.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import java.util.regex.Pattern;

/** 로그 메시지 내의 비밀값, 인증 토큰 등 민감 정보를 정규식으로 마스킹하는 Logback 컨버터. */
public class MaskingMessageConverter extends CompositeConverter<ILoggingEvent> {

  private static final String MASKED = "******";

  private static final Pattern BEARER_PATTERN =
      Pattern.compile("(?i)\\b(Bearer[ \\t]+)[A-Za-z0-9._~+/=-]+");

  private static final Pattern KEY_VALUE_PATTERN =
      Pattern.compile(
          "(?i)([\"']?\\b(?:password|passwd|secret|token|apiKey|authorization|accessToken|refreshToken)\\b[\"']?\\s*[:=]\\s*)([\"']?)([^\"'\\s,&]+)([\"']?)");

  private static final Pattern JWT_PATTERN =
      Pattern.compile("\\beyJ[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=+/]*\\b");

  @Override
  protected String transform(ILoggingEvent event, String in) {
    if (in == null || in.isEmpty()) {
      return in;
    }
    return mask(in);
  }

  /**
   * 문자열 내의 민감한 인증 정보 및 비밀값을 감지하여 마스킹한다.
   *
   * @param message 원본 로그 메시지
   * @return 민감 정보가 마스킹된 로그 메시지
   */
  public static String mask(String message) {
    if (message == null || message.isEmpty()) {
      return message;
    }

    String masked = BEARER_PATTERN.matcher(message).replaceAll("$1" + MASKED);
    masked = KEY_VALUE_PATTERN.matcher(masked).replaceAll("$1$2" + MASKED + "$4");
    masked = JWT_PATTERN.matcher(masked).replaceAll(MASKED);
    return masked;
  }
}
