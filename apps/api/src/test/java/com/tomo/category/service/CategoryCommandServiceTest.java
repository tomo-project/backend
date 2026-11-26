package com.tomo.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tomo.category.entity.Category;
import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.repository.CategoryRepository;
import com.tomo.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {
  private static final String USER_ID = "user-id";

  @Mock private CategoryRepository categoryRepository;

  private CategoryCommandService service;

  @BeforeEach
  void setUp() {
    service = new CategoryCommandService(categoryRepository);
  }

  @Test
  @DisplayName("유효하고 중복되지 않은 카테고리 이름이면 저장소에 저장한다")
  void createsCategory() {
    // given
    when(categoryRepository.existsByUserIdAndCategoryName(USER_ID, "전공")).thenReturn(false);

    // when
    service.createCategory(USER_ID, "전공");

    // then
    ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).saveAndFlush(captor.capture());
    Category saved = captor.getValue();
    assertThat(saved.getUserId()).isEqualTo(USER_ID);
    assertThat(saved.getCategoryName()).isEqualTo("전공");
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
  }

  @Test
  @DisplayName("카테고리 이름이 null이면 INVALID_CATEGORY_NAME을 던진다")
  void rejectsNullCategoryName() {
    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, null));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(categoryRepository);
  }

  @Test
  @DisplayName("카테고리 이름이 공백이면 INVALID_CATEGORY_NAME을 던진다")
  void rejectsBlankCategoryName() {
    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, "   "));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(categoryRepository);
  }

  @Test
  @DisplayName("카테고리 이름에 앞뒤 공백이 있으면 INVALID_CATEGORY_NAME을 던진다")
  void rejectsCategoryNameWithSurroundingWhitespace() {
    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, " 전공 "));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(categoryRepository);
  }

  @Test
  @DisplayName("카테고리 이름이 100자를 초과하면 INVALID_CATEGORY_NAME을 던진다")
  void rejectsCategoryNameExceedingMaxLength() {
    // given
    String tooLongName = "가".repeat(101);

    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, tooLongName));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.INVALID_CATEGORY_NAME));

    verifyNoInteractions(categoryRepository);
  }

  @Test
  @DisplayName("동일한 사용자에게 같은 이름의 카테고리가 존재하면 CATEGORY_ALREADY_EXISTS를 던진다")
  void rejectsDuplicateCategoryName() {
    // given
    when(categoryRepository.existsByUserIdAndCategoryName(USER_ID, "전공")).thenReturn(true);

    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, "전공"));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.CATEGORY_ALREADY_EXISTS));
  }

  @Test
  @DisplayName("동시 요청으로 DB 유니크 제약 충돌이 발생하면 CONCURRENT_REQUEST_CONFLICT를 던진다")
  void translatesUniqueConstraintViolationToConcurrentConflict() {
    // given
    when(categoryRepository.existsByUserIdAndCategoryName(USER_ID, "전공")).thenReturn(false);
    when(categoryRepository.saveAndFlush(any(Category.class)))
        .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

    // when
    Throwable thrown = catchThrowable(() -> service.createCategory(USER_ID, "전공"));

    // then
    assertThat(thrown)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(CategoryErrorCode.CONCURRENT_REQUEST_CONFLICT));
  }
}
