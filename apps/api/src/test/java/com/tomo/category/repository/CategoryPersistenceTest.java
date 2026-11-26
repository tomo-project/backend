package com.tomo.category.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tomo.category.entity.Category;
import com.tomo.user.entity.User;
import com.tomo.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
    // then
    assertThatThrownBy(() -> categoryRepository.saveAndFlush(new Category(user.getId(), "전공")))
        .isInstanceOf(DataIntegrityViolationException.class);
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
}
