package com.tomo.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tomo.common.exception.BusinessException;
import com.tomo.common.exception.ErrorCode;
import com.tomo.todo.entity.Todo;
import com.tomo.todo.entity.TodoPriority;
import com.tomo.todo.repository.TodoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodoAccessServiceTest {
  @Mock private TodoRepository todoRepository;
  private TodoAccessService service;

  @BeforeEach
  void setUp() {
    service = new TodoAccessService(todoRepository);
  }

  @Test
  @DisplayName("본인 소유의 삭제되지 않은 To-do는 작업 대상으로 반환한다")
  void returnsOwnedTodo() {
    // given
    Todo todo =
        Todo.create(
            "owner",
            "공부",
            "학습",
            TodoPriority.HIGH,
            LocalDate.of(2026, 11, 16),
            30,
            null,
            LocalDateTime.of(2026, 11, 16, 0, 0));
    when(todoRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(todo));

    // when
    Todo result = service.getOwnedTodo("owner", 1L);

    // then
    assertThat(result).isSameAs(todo);
  }

  @Test
  @DisplayName("다른 사용자 소유의 To-do는 공통 권한 오류로 거부한다")
  void rejectsOtherOwnersTodo() {
    // given
    Todo todo =
        Todo.create(
            "owner",
            "공부",
            "학습",
            TodoPriority.HIGH,
            LocalDate.of(2026, 11, 16),
            30,
            null,
            LocalDateTime.of(2026, 11, 16, 0, 0));
    when(todoRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(todo));

    // when
    // then
    assertThatThrownBy(() -> service.getOwnedTodo("another-user", 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.FORBIDDEN);
    assertThat(ErrorCode.FORBIDDEN.getHttpStatus()).isEqualTo(403);
  }

  @Test
  @DisplayName("삭제되지 않은 To-do 조회 결과가 없으면 미존재 오류로 거부한다")
  void rejectsMissingTodo() {
    // given
    when(todoRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> service.getOwnedTodo("owner", 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    assertThat(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).isEqualTo(404);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(longs = {0L, -1L})
  @DisplayName("누락 또는 양수가 아닌 To-do ID는 DB 조회 전에 거부한다")
  void rejectsInvalidTodoId(Long todoId) {
    // when
    // then
    assertThatThrownBy(() -> service.getOwnedTodo("owner", todoId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_REQUEST);
    verifyNoInteractions(todoRepository);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " "})
  @DisplayName("누락 또는 공백 사용자 ID는 DB 조회 전에 거부한다")
  void rejectsInvalidUserId(String userId) {
    // when
    // then
    assertThatThrownBy(() -> service.getOwnedTodo(userId, 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_REQUEST);
    verifyNoInteractions(todoRepository);
  }
}
