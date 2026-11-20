package com.tomo.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.WebUtils;

class RequestIdFilterTest {
  private final RequestIdFilter filter = new RequestIdFilter();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 401, 403, 500})
  @DisplayName("정상·인증 실패·서버 오류 응답은 같은 요청 ID를 사용하고 MDC를 정리한다")
  void bindsRequestIdUntilResponseCompletes(int status) throws Exception {
    // given
    MockHttpServletRequest request = spy(new MockHttpServletRequest());
    when(request.getRequestId()).thenReturn("request-" + status);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MDC.put("other", "preserved");

    // when
    filter.doFilter(
        request,
        response,
        (req, res) -> {
          assertThat(MDC.get("requestId")).isEqualTo(request.getRequestId());
          response.setStatus(status);
          response.getWriter().write(request.getRequestId());
        });

    // then
    assertThat(response.getContentAsString()).isEqualTo("request-" + status);
    assertThat(MDC.getCopyOfContextMap()).containsExactlyEntriesOf(Map.of("other", "preserved"));
  }

  @Test
  @DisplayName("예외가 전파되어도 요청 ID를 제거하고 다음 요청에 새 ID를 적용한다")
  void cleansUpAfterFailure() throws Exception {
    // given
    MockHttpServletRequest failedRequest = spy(new MockHttpServletRequest());
    when(failedRequest.getRequestId()).thenReturn("failed-request");

    // when
    assertThatThrownBy(
            () ->
                filter.doFilter(
                    failedRequest,
                    new MockHttpServletResponse(),
                    (req, res) -> {
                      throw new ServletException("failure");
                    }))
        .isInstanceOf(ServletException.class);

    // then
    assertThat(MDC.get("requestId")).isNull();
    MockHttpServletRequest nextRequest = spy(new MockHttpServletRequest());
    when(nextRequest.getRequestId()).thenReturn("next-request");
    filter.doFilter(
        nextRequest,
        new MockHttpServletResponse(),
        (req, res) -> assertThat(MDC.get("requestId")).isEqualTo("next-request"));
    assertThat(MDC.get("requestId")).isNull();
  }

  @ParameterizedTest
  @EnumSource(
      value = DispatcherType.class,
      names = {"ASYNC", "ERROR"})
  @DisplayName("비동기·오류 재디스패치에서도 동일한 요청 ID를 등록하고 정리한다")
  void bindsRedispatch(DispatcherType dispatcherType) throws Exception {
    // given
    MockHttpServletRequest request = spy(new MockHttpServletRequest());
    when(request.getRequestId()).thenReturn("redispatched-request");
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> {});
    request.setDispatcherType(dispatcherType);
    if (dispatcherType == DispatcherType.ERROR) {
      request.setAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE, "/failed");
    }

    // when
    filter.doFilter(
        request,
        response,
        (req, res) -> assertThat(MDC.get("requestId")).isEqualTo("redispatched-request"));

    // then
    assertThat(MDC.get("requestId")).isNull();
  }

  @Test
  @DisplayName("중첩 오류 디스패치 후에도 외부 요청 ID와 기존 MDC 값을 보존한다")
  void preservesContextForNestedErrorDispatch() throws Exception {
    // given
    MockHttpServletRequest request = spy(new MockHttpServletRequest());
    when(request.getRequestId()).thenReturn("nested-request");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MDC.put("requestId", "outer-context");

    // when
    filter.doFilter(
        request,
        response,
        (req, res) -> {
          request.setDispatcherType(DispatcherType.ERROR);
          request.setAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE, "/failed");
          filter.doFilter(
              request,
              response,
              (nestedReq, nestedRes) ->
                  assertThat(MDC.get("requestId")).isEqualTo("nested-request"));
          assertThat(MDC.get("requestId")).isEqualTo("nested-request");
        });

    // then
    assertThat(MDC.get("requestId")).isEqualTo("outer-context");
  }

  @Test
  @DisplayName("Security 필터 순서를 변경해도 요청 ID 필터가 인증보다 먼저 실행된다")
  void registersBeforeSecurity() {
    // given
    SecurityProperties properties = new SecurityProperties();
    properties.getFilter().setOrder(-200);

    // when
    var registration = new RequestLoggingConfig().requestIdFilter(properties);

    // then
    assertThat(registration.getOrder()).isEqualTo(-201);
    assertThat(registration.isAsyncSupported()).isTrue();
  }
}
