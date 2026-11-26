package com.tomo.category.repository;

import com.tomo.category.entity.Category;
import com.tomo.category.service.model.CategoryItemResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 공부 카테고리 영속성 접근 계약. */
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * 동일한 사용자에게 같은 이름의 카테고리가 이미 존재하는지 확인한다.
   *
   * @param userId 사용자 ID
   * @param categoryName 카테고리 이름
   * @return 동일한 이름의 카테고리가 존재하면 true
   */
  boolean existsByUserIdAndCategoryName(String userId, String categoryName);

  /**
   * 사용자의 공부 카테고리 목록을 ID 오름차순으로 조회한다.
   *
   * @param userId 사용자 ID
   * @return 사용자의 공부 카테고리 목록
   */
  @Query(
      "select new com.tomo.category.service.model.CategoryItemResult(c.id, c.categoryName) "
          + "from Category c where c.userId = :userId order by c.id asc")
  List<CategoryItemResult> findAllItemsByUserIdOrderByIdAsc(@Param("userId") String userId);
}
