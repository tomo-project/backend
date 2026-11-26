package com.tomo.category.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.service.CategoryFacade;
import com.tomo.category.service.command.CreateCategoryCommand;
import com.tomo.common.auth.config.SecurityConfig;
import com.tomo.common.auth.token.AccessTokenProvider;
import com.tomo.common.exception.BusinessException;
import com.tomo.user.service.LoginSessionService;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, CategoryControllerTest.JwtTestConfig.class})
@ActiveProfiles("test")
class CategoryControllerTest {
  private static final String PATH = "/api/v1/category";
  private static final String USER_ID = "user-id";
  private static final long SESSION_ID = 10L;
  private static final Instant NOW = Instant.parse("2026-11-26T00:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private AccessTokenProvider accessTokenProvider;

  @MockitoBean private LoginSessionService loginSessionService;
  @MockitoBean private CategoryFacade categoryFacade;

  @Test
  @DisplayName("유효한 JWT와 카테고리 이름으로 요청하면 200과 data 없는 성공 응답을 반환한다")
  void createsCategory() throws Exception {
    // given
    String token = accessToken();
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"전공\"}"));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andExpect(jsonPath("$.meta.requestId").exists())
        .andExpect(jsonPath("$.meta.timestamp").isNotEmpty());

    verify(categoryFacade).createCategory(new CreateCategoryCommand(USER_ID, "전공"));
  }

  @Test
  @DisplayName("카테고리 필드가 누락되면 400 INVALID_CATEGORY_NAME을 반환한다")
  void rejectsMissingCategoryField() throws Exception {
    // given
    String token = accessToken();
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY_NAME"));

    verifyNoInteractions(categoryFacade);
  }

  @Test
  @DisplayName("카테고리 이름이 공백이면 400 INVALID_CATEGORY_NAME을 반환한다")
  void rejectsBlankCategoryName() throws Exception {
    // given
    String token = accessToken();
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"   \"}"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY_NAME"));

    verifyNoInteractions(categoryFacade);
  }

  @Test
  @DisplayName("카테고리 이름에 앞뒤 공백이 있으면 400 INVALID_CATEGORY_NAME을 반환한다")
  void rejectsCategoryNameWithSurroundingWhitespace() throws Exception {
    // given
    String token = accessToken();
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\" 전공 \"}"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY_NAME"));

    verifyNoInteractions(categoryFacade);
  }

  @Test
  @DisplayName("카테고리 이름이 100자를 초과하면 400 INVALID_CATEGORY_NAME을 반환한다")
  void rejectsTooLongCategoryName() throws Exception {
    // given
    String token = accessToken();
    String tooLongName = "가".repeat(101);
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"" + tooLongName + "\"}"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY_NAME"));

    verifyNoInteractions(categoryFacade);
  }

  @Test
  @DisplayName("Access Token이 없으면 401 INVALID_ACCESS_TOKEN을 반환한다")
  void rejectsMissingToken() throws Exception {
    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH).contentType(MediaType.APPLICATION_JSON).content("{\"category\":\"전공\"}"));

    // then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));

    verifyNoInteractions(categoryFacade);
  }

  @Test
  @DisplayName("생성 권한이 없으면 403 CATEGORY_CREATE_FORBIDDEN을 반환한다")
  void returnsCategoryCreateForbidden() throws Exception {
    // given
    String token = accessToken();
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "전공");
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);
    doThrow(new BusinessException(CategoryErrorCode.CATEGORY_CREATE_FORBIDDEN))
        .when(categoryFacade)
        .createCategory(command);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"전공\"}"));

    // then
    result
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("CATEGORY_CREATE_FORBIDDEN"));
  }

  @Test
  @DisplayName("동일한 이름의 카테고리가 이미 존재하면 409 CATEGORY_ALREADY_EXISTS를 반환한다")
  void returnsCategoryAlreadyExists() throws Exception {
    // given
    String token = accessToken();
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "전공");
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);
    doThrow(new BusinessException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS))
        .when(categoryFacade)
        .createCategory(command);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"전공\"}"));

    // then
    result
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("CATEGORY_ALREADY_EXISTS"));
  }

  @Test
  @DisplayName("동시 요청 충돌이 발생하면 409 CONCURRENT_REQUEST_CONFLICT를 반환한다")
  void returnsConcurrentRequestConflict() throws Exception {
    // given
    String token = accessToken();
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "전공");
    when(loginSessionService.isValidAccessSession(USER_ID, SESSION_ID)).thenReturn(true);
    doThrow(new BusinessException(CategoryErrorCode.CONCURRENT_REQUEST_CONFLICT))
        .when(categoryFacade)
        .createCategory(command);

    // when
    ResultActions result =
        mockMvc.perform(
            post(PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"전공\"}"));

    // then
    result
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("CONCURRENT_REQUEST_CONFLICT"));
  }

  private String accessToken() {
    return accessTokenProvider.issue(USER_ID, SESSION_ID, NOW.plusSeconds(600));
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class JwtTestConfig {
    @Bean
    AccessTokenProvider accessTokenProvider() {
      return new AccessTokenProvider(
          Jwts.SIG.HS256.key().build(), Clock.fixed(NOW, ZoneOffset.UTC));
    }
  }
}
