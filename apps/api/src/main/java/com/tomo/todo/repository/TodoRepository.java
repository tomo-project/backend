package com.tomo.todo.repository;

import com.tomo.todo.entity.Todo;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/** 기록 보존을 위해 물리 삭제를 노출하지 않고 To-do 저장과 조회만 제공한다. */
public interface TodoRepository extends Repository<Todo, Long> {
  /**
   * To-do를 저장한다.
   *
   * @param <S> 저장할 To-do 타입
   * @param entity 저장할 To-do
   * @return 저장된 To-do
   */
  <S extends Todo> S save(S entity);

  /**
   * To-do를 저장하고 변경 내용을 DB에 반영한다.
   *
   * @param <S> 저장할 To-do 타입
   * @param entity 저장할 To-do
   * @return 저장된 To-do
   */
  <S extends Todo> S saveAndFlush(S entity);

  /**
   * 삭제되지 않은 To-do를 조회한다. 소유권은 호출 측에서 별도로 검증한다.
   *
   * @param id To-do 식별자
   * @return 삭제되지 않은 To-do
   */
  Optional<Todo> findByIdAndDeletedAtIsNull(Long id);

  /**
   * 사용자 소유의 삭제되지 않은 To-do를 조회한다.
   *
   * @param id To-do 식별자
   * @param userId 소유자 식별자
   * @return 조건에 맞는 To-do
   */
  Optional<Todo> findByIdAndUserIdAndDeletedAtIsNull(Long id, String userId);
}
