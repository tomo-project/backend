package com.tomo.todo.service;

import com.tomo.common.exception.BusinessException;
import com.tomo.common.exception.ErrorCode;
import com.tomo.todo.entity.Todo;
import com.tomo.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** To-do 소유권을 검증하고 삭제되지 않은 대상을 조회한다. */
@Service
@Transactional(readOnly = true)
public class TodoAccessService {
  private final TodoRepository todoRepository;

  /**
   * To-do 조회에 필요한 저장소를 주입한다.
   *
   * @param todoRepository To-do 저장소
   */
  public TodoAccessService(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  /**
   * 삭제되지 않은 To-do를 조회하고 인증된 사용자의 소유인지 확인한다.
   *
   * @param userId 인증 계층에서 전달한 사용자 식별자
   * @param todoId To-do 식별자
   * @return 사용자 소유의 삭제되지 않은 To-do
   * @throws BusinessException 입력이 유효하지 않으면 INVALID_REQUEST, 대상이 없으면 RESOURCE_NOT_FOUND, 소유자가 아니면
   *     FORBIDDEN
   */
  public Todo getOwnedTodo(String userId, Long todoId) {
    if (userId == null || userId.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (todoId == null || todoId <= 0) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }

    Todo todo =
        todoRepository
            .findByIdAndDeletedAtIsNull(todoId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    if (!todo.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    return todo;
  }
}
