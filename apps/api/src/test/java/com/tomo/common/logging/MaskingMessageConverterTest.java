package com.tomo.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MaskingMessageConverterTest {

  private final MaskingMessageConverter converter = new MaskingMessageConverter();

  @Test
  @DisplayName("일반적인 로그 메시지는 변경 없이 그대로 유지된다")
  void keepsNormalMessageUnchanged() {
    String message = "Server started successfully on port 8080 with 5 worker threads.";
    assertThat(MaskingMessageConverter.mask(message)).isEqualTo(message);
  }

  @Test
  @DisplayName("null 또는 빈 문자열은 그대로 반환된다")
  void handlesNullOrEmpty() {
    assertThat(MaskingMessageConverter.mask(null)).isNull();
    assertThat(MaskingMessageConverter.mask("")).isEmpty();
  }

  @Test
  @DisplayName("등호(=)로 연결된 패스워드는 마스킹된다")
  void masksPasswordWithEqualsSign() {
    String input = "user login attempt with password=secret1234";
    assertThat(MaskingMessageConverter.mask(input))
        .isEqualTo("user login attempt with password=******");
  }

  @Test
  @DisplayName("콜론(:)으로 연결된 패스워드는 마스킹된다")
  void masksPasswordWithColon() {
    String input = "payload: password: secret1234";
    assertThat(MaskingMessageConverter.mask(input)).isEqualTo("payload: password: ******");
  }

  @Test
  @DisplayName("JSON 큰따옴표 형식의 패스워드는 마스킹된다")
  void masksPasswordInDoubleQuotes() {
    String input = "{\"password\": \"secret1234\"}";
    assertThat(MaskingMessageConverter.mask(input)).isEqualTo("{\"password\": \"******\"}");
  }

  @Test
  @DisplayName("작은따옴표 형식의 패스워드는 마스킹된다")
  void masksPasswordInSingleQuotes() {
    String input = "{'password': 'secret1234'}";
    assertThat(MaskingMessageConverter.mask(input)).isEqualTo("{'password': '******'}");
  }

  @Test
  @DisplayName("API 키와 시크릿 키는 마스킹된다")
  void masksApiKeyAndSecret() {
    String input = "secret=my-api-secret-key, apiKey=ai-key-secret-999";
    assertThat(MaskingMessageConverter.mask(input)).isEqualTo("secret=******, apiKey=******");
  }

  @Test
  @DisplayName("액세스 토큰 및 리프레시 토큰은 마스킹된다")
  void masksAccessAndRefreshTokens() {
    String input = "accessToken=xyz789&refreshToken=refresh-token-val";
    assertThat(MaskingMessageConverter.mask(input))
        .isEqualTo("accessToken=******&refreshToken=******");
  }

  @Test
  @DisplayName("Bearer 인증 헤더의 토큰 문자열은 마스킹된다")
  void masksBearerToken() {
    String input = "Authorization header: Bearer abc123def456ghi789";
    String masked = MaskingMessageConverter.mask(input);
    assertThat(masked).isEqualTo("Authorization header: Bearer ******");
  }

  @Test
  @DisplayName("JWT 형식의 토큰 문자열은 독립적으로 마스킹된다")
  void masksJwtToken() {
    String jwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.doNotLeakThisSignature";
    String input = "Failed authentication for JWT: " + jwt;
    String masked = MaskingMessageConverter.mask(input);
    assertThat(masked).isEqualTo("Failed authentication for JWT: ******");
  }

  @Test
  @DisplayName("여러 종류의 민감 정보가 포함된 로그에서도 모두 마스킹된다")
  void masksMultipleSensitiveFields() {
    String input =
        "User login failed. username=test-user, password=plainPassword123, auth=Bearer eyJhbGci.eyJzdWI.sig";
    String masked = MaskingMessageConverter.mask(input);

    assertThat(masked).doesNotContain("plainPassword123");
    assertThat(masked).doesNotContain("Bearer eyJhbGci.eyJzdWI.sig");
    assertThat(masked).contains("password=******");
    assertThat(masked).contains("auth=Bearer ******");
    assertThat(masked).contains("username=test-user");
  }

  @Test
  @DisplayName("Logback 컨버터 체인 동작 시 transform 메서드로 마스킹된 결과를 반환한다")
  void transformsThroughConverter() {
    ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
    String input = "Connecting with password=super-secret";
    String result = converter.transform(event, input);
    assertThat(result).isEqualTo("Connecting with password=******");
  }
}
