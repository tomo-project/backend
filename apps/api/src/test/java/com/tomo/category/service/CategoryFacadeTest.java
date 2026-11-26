package com.tomo.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.service.command.CreateCategoryCommand;
import com.tomo.common.auth.exception.AuthErrorCode;
import com.tomo.common.exception.BusinessException;
import com.tomo.user.service.UserCategoryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryFacadeTest {
  private static final String USER_ID = "user-id";

  @Mock private UserCategoryQueryService userCategoryQueryService;
  @Mock private CategoryCommandService categoryCommandService;

  private CategoryFacade facade;

  @BeforeEach
  void setUp() {
    facade = new CategoryFacade(userCategoryQueryService, categoryCommandService);
  }

  @Test
  @DisplayName("활성 사용자이고 유효한 카테고리 이름이면 카테고리 생성 서비스를 호출한다")
  void createsCategoryForActiveUser() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "전공");
    when(userCategoryQueryService.isActiveCategoryUser(USER_ID)).thenReturn(true);

    // when
    facade.createCategory(command);

    // then
    verify(categoryCommandService).createCategory(USER_ID, "전공");
  }

  @Test
  @DisplayName("활성 상태가 아닌 사용자는 CATEGORY_CREATE_FORBIDDEN으로 거부한다")
  void rejectsInactiveUser() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "전공");
    when(userCategoryQueryService.isActiveCategoryUser(USER_ID)).thenReturn(false);

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.CATEGORY_CREATE_FORBIDDEN));

    verifyNoInteractions(categoryCommandService);
  }

  @Test
  @DisplayName("사용자 ID가 null이면 INVALID_ACCESS_TOKEN으로 거부한다")
  void rejectsNullUserId() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(null, "전공");

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN));

    verifyNoInteractions(userCategoryQueryService, categoryCommandService);
  }

  @Test
  @DisplayName("사용자 ID가 공백이면 INVALID_ACCESS_TOKEN으로 거부한다")
  void rejectsBlankUserId() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand("   ", "전공");

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN));

    verifyNoInteractions(userCategoryQueryService, categoryCommandService);
  }

  @Test
  @DisplayName("카테고리 이름이 null이면 사용자 조회 전에 INVALID_CATEGORY_NAME으로 거부한다")
  void rejectsNullCategoryNameBeforeUserCheck() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, null);

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(userCategoryQueryService, categoryCommandService);
  }

  @Test
  @DisplayName("카테고리 이름이 공백이면 사용자 조회 전에 INVALID_CATEGORY_NAME으로 거부한다")
  void rejectsBlankCategoryNameBeforeUserCheck() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, "   ");

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(userCategoryQueryService, categoryCommandService);
  }

  @Test
  @DisplayName("카테고리 이름에 앞뒤 공백이 있으면 사용자 조회 전에 INVALID_CATEGORY_NAME으로 거부한다")
  void rejectsCategoryNameWithSurroundingWhitespaceBeforeUserCheck() {
    // given
    CreateCategoryCommand command = new CreateCategoryCommand(USER_ID, " 전공 ");

    // when
    Throwable thrown = catchThrowable(() -> facade.createCategory(command));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(userCategoryQueryService, categoryCommandService);
  }
}
