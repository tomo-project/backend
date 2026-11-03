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
    // given
    String message = "Server started successfully on port 8080 with 5 worker threads.";

    // when
    String masked = MaskingMessageConverter.mask(message);

    // then
    assertThat(masked).isEqualTo(message);
  }

  @Test
  @DisplayName("null 또는 빈 문자열은 그대로 반환된다")
  void handlesNullOrEmpty() {
    // given
    String nullInput = null;
    String emptyInput = "";

    // when
    String maskedNull = MaskingMessageConverter.mask(nullInput);
    String maskedEmpty = MaskingMessageConverter.mask(emptyInput);

    // then
    assertThat(maskedNull).isNull();
    assertThat(maskedEmpty).isEmpty();
  }

  @Test
  @DisplayName("등호(=)로 연결된 패스워드는 마스킹된다")
  void masksPasswordWithEqualsSign() {
    // given
    String input = "user login attempt with password=secret1234";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("user login attempt with password=******");
  }

  @Test
  @DisplayName("콜론(:)으로 연결된 패스워드는 마스킹된다")
  void masksPasswordWithColon() {
    // given
    String input = "payload: password: secret1234";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("payload: password: ******");
  }

  @Test
  @DisplayName("JSON 큰따옴표 형식의 패스워드는 마스킹된다")
  void masksPasswordInDoubleQuotes() {
    // given
    String input = "{\"password\": \"secret1234\"}";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("{\"password\": \"******\"}");
  }

  @Test
  @DisplayName("작은따옴표 형식의 패스워드는 마스킹된다")
  void masksPasswordInSingleQuotes() {
    // given
    String input = "{'password': 'secret1234'}";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("{'password': '******'}");
  }

  @Test
  @DisplayName("API 키와 시크릿 키는 마스킹된다")
  void masksApiKeyAndSecret() {
    // given
    String input = "secret=my-api-secret-key, apiKey=ai-key-secret-999";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("secret=******, apiKey=******");
  }

  @Test
  @DisplayName("액세스 토큰 및 리프레시 토큰은 마스킹된다")
  void masksAccessAndRefreshTokens() {
    // given
    String input = "accessToken=xyz789&refreshToken=refresh-token-val";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("accessToken=******&refreshToken=******");
  }

  @Test
  @DisplayName("Bearer 인증 헤더의 토큰 문자열은 마스킹된다")
  void masksBearerToken() {
    // given
    String input = "Authorization header: Bearer abc123def456ghi789";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("Authorization header: Bearer ******");
  }

  @Test
  @DisplayName("Bearer 토큰의 슬래시와 더하기 및 물결표와 패딩까지 모두 마스킹된다")
  void masksCompleteBearerToken() {
    // given
    String input = "Authorization header: bEaReR abc/def+ghi~jkl.mno_pqr-stu==, requestId=123";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("Authorization header: bEaReR ******, requestId=123");
  }

  @Test
  @DisplayName("JWT 형식의 토큰 문자열은 독립적으로 마스킹된다")
  void masksJwtToken() {
    // given
    String dummyHeader = "eyJ" + "hbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    String dummyPayload = "eyJ" + "zdWIiOiIxMjM0NTY3ODkwIn0";
    String dummySignature = "doNotLeakThisSignature";
    String jwt = String.join(".", dummyHeader, dummyPayload, dummySignature);
    String input = "Failed authentication for JWT: " + jwt;

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).isEqualTo("Failed authentication for JWT: ******");
  }

  @Test
  @DisplayName("여러 종류의 민감 정보가 포함된 로그에서도 모두 마스킹된다")
  void masksMultipleSensitiveFields() {
    // given
    String input =
        "User login failed. username=test-user, password=plainPassword123, auth=Bearer eyJhbGci.eyJzdWI.sig";

    // when
    String masked = MaskingMessageConverter.mask(input);

    // then
    assertThat(masked).doesNotContain("plainPassword123");
    assertThat(masked).doesNotContain("Bearer eyJhbGci.eyJzdWI.sig");
    assertThat(masked).contains("password=******");
    assertThat(masked).contains("auth=Bearer ******");
    assertThat(masked).contains("username=test-user");
  }

  @Test
  @DisplayName("Logback 컨버터 체인 동작 시 transform 메서드로 마스킹된 결과를 반환한다")
  void transformsThroughConverter() {
    // given
    ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
    String input = "Connecting with password=super-secret";

    // when
    String result = converter.transform(event, input);

    // then
    assertThat(result).isEqualTo("Connecting with password=******");
  }
}
