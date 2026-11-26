package com.tomo.category.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.tomo.category.entity.Category;
import com.tomo.category.service.model.CategoryItemResult;
import com.tomo.user.entity.User;
import com.tomo.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryPersistenceTest {
  @Autowired private EntityManager entityManager;
  @Autowired private UserRepository userRepository;
  @Autowired private CategoryRepository categoryRepository;

  @Test
  @DisplayName("신규 카테고리는 AUTO_INCREMENT ID를 발급받아 저장되고 사용자별 이름 존재 여부를 조회한다")
  void savesCategoryWithAutoIncrementId() {
    // given
    User user = userRepository.saveAndFlush(User.createPending());

    // when
    Category saved = categoryRepository.saveAndFlush(new Category(user.getId(), "전공"));
    entityManager.clear();

    // then
    assertThat(saved.getId()).isNotNull().isPositive();
    assertThat(categoryRepository.existsByUserIdAndCategoryName(user.getId(), "전공")).isTrue();
    assertThat(categoryRepository.existsByUserIdAndCategoryName(user.getId(), "교양")).isFalse();
  }

  @Test
  @DisplayName("같은 사용자가 동일한 이름의 카테고리를 중복 저장하면 유니크 제약 위반이 발생한다")
  void rejectsDuplicateCategoryForSameUser() {
    // given
    User user = userRepository.saveAndFlush(User.createPending());
    categoryRepository.saveAndFlush(new Category(user.getId(), "전공"));

    // when
    Throwable thrown =
        catchThrowable(() -> categoryRepository.saveAndFlush(new Category(user.getId(), "전공")));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("서로 다른 사용자는 동일한 이름의 카테고리를 각각 저장할 수 있다")
  void allowsSameCategoryNameForDifferentUsers() {
    // given
    User firstUser = userRepository.saveAndFlush(User.createPending());
    User secondUser = userRepository.saveAndFlush(User.createPending());

    // when
    Category firstSaved = categoryRepository.saveAndFlush(new Category(firstUser.getId(), "전공"));
    Category secondSaved = categoryRepository.saveAndFlush(new Category(secondUser.getId(), "전공"));
    entityManager.clear();

    // then
    assertThat(firstSaved.getId()).isNotEqualTo(secondSaved.getId());
    assertThat(categoryRepository.existsByUserIdAndCategoryName(firstUser.getId(), "전공")).isTrue();
    assertThat(categoryRepository.existsByUserIdAndCategoryName(secondUser.getId(), "전공")).isTrue();
  }

  @Test
  @DisplayName("사용자의 카테고리 목록을 ID 오름차순으로 조회한다")
  void findsAllCategoriesByUserIdOrderedByIdAsc() {
    // given
    User owner = userRepository.saveAndFlush(User.createPending());
    User otherUser = userRepository.saveAndFlush(User.createPending());

    Category first = categoryRepository.saveAndFlush(new Category(owner.getId(), "전공"));
    Category second = categoryRepository.saveAndFlush(new Category(owner.getId(), "교양"));
    categoryRepository.saveAndFlush(new Category(otherUser.getId(), "자격증"));
    entityManager.clear();

    // when
    List<CategoryItemResult> results =
        categoryRepository.findAllItemsByUserIdOrderByIdAsc(owner.getId());

    // then
    assertThat(results)
        .containsExactly(
            new CategoryItemResult(first.getId(), "전공"),
            new CategoryItemResult(second.getId(), "교양"));
  }
}
