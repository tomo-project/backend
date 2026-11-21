package com.tomo.todo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TodoRepositoryContractTest {
  @Test
  @DisplayName("기록 보존을 위해 Repository는 상속된 메서드를 포함해 물리 삭제를 노출하지 않는다")
  void doesNotExposePhysicalDeletion() {
    // when
    String[] methodNames =
        Arrays.stream(TodoRepository.class.getMethods())
            .map(Method::getName)
            .toArray(String[]::new);
    // then
    assertThat(methodNames).noneMatch(name -> name.startsWith("delete"));
  }
}
