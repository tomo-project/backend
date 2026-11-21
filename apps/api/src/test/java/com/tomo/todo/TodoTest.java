package com.tomo.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tomo.todo.entity.Todo;
import com.tomo.todo.entity.TodoPriority;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TodoTest {
  @Test
  @DisplayName("신규 To-do는 미완료·미삭제 상태이며 알림 없이 생성할 수 있다")
  void createsIncompleteTodoWithoutReminder() {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 11, 13, 12, 0);
    // when
    Todo todo =
        Todo.create(
            "owner", "자료구조", "전공", TodoPriority.HIGH, LocalDate.of(2026, 11, 14), 60, null, now);
    // then
    assertThat(todo.isCompleted()).isFalse();
    assertThat(todo.isDeleted()).isFalse();
    assertThat(todo.getReminderAt()).isNull();
    assertThat(todo.getCreatedAt()).isEqualTo(now);
    assertThat(todo.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("소유자만 일치하며 타인과 식별값이 없는 사용자는 일치하지 않는다")
  void checksOwnership() {
    // given
    Todo todo =
        Todo.create(
            "owner",
            "자료구조",
            "전공",
            TodoPriority.HIGH,
            LocalDate.of(2026, 11, 14),
            60,
            null,
            LocalDateTime.of(2026, 11, 13, 12, 0));
    // then
    assertThat(todo.isOwnedBy("owner")).isTrue();
    assertThat(todo.isOwnedBy("other")).isFalse();
    assertThat(todo.isOwnedBy(null)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  @DisplayName("양수가 아닌 예상 학습시간은 생성 시 거부한다")
  void rejectsNonpositiveEstimate(int estimatedMinutes) {
    // when
    // then
    assertThatThrownBy(
            () ->
                Todo.create(
                    "owner",
                    "자료구조",
                    "전공",
                    TodoPriority.HIGH,
                    LocalDate.of(2026, 11, 14),
                    estimatedMinutes,
                    null,
                    LocalDateTime.of(2026, 11, 13, 12, 0)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("예상 학습시간은 0보다 커야 한다.");
  }

  @Test
  @DisplayName("예상 학습시간의 최소 양수인 1분으로 생성할 수 있다")
  void acceptsMinimumPositiveEstimate() {
    // when
    Todo todo =
        Todo.create(
            "owner",
            "자료구조",
            "전공",
            TodoPriority.HIGH,
            LocalDate.of(2026, 11, 14),
            1,
            null,
            LocalDateTime.of(2026, 11, 13, 12, 0));
    // then
    assertThat(todo.getEstimatedMinutes()).isEqualTo(1);
  }
}
