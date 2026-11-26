package com.tomo.category.service;

import com.tomo.category.entity.Category;
import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.repository.CategoryRepository;
import com.tomo.common.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공부 카테고리의 생성·수정·삭제를 수행한다. */
@Service
public class CategoryCommandService {
  /** 카테고리 이름 최대 길이. */
  public static final int MAX_NAME_LENGTH = 100;

  private final CategoryRepository categoryRepository;

  /**
   * 카테고리 저장소를 주입받는다.
   *
   * @param categoryRepository 카테고리 저장소
   */
  public CategoryCommandService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * 사용자 소유의 공부 카테고리를 생성한다.
   *
   * @param userId 인증된 사용자 ID
   * @param categoryName 생성할 카테고리 이름
   * @throws BusinessException 이름 형식이 잘못되었거나 이미 동일한 이름의 카테고리가 존재하거나 동시 요청이 충돌한 경우
   */
  @Transactional
  public void createCategory(String userId, String categoryName) {
    validateCategoryName(categoryName);

    if (categoryRepository.existsByUserIdAndCategoryName(userId, categoryName)) {
      throw new BusinessException(CategoryErrorCode.CATEGORY_ALREADY_EXISTS);
    }

    Category category = new Category(userId, categoryName);

    try {
      categoryRepository.saveAndFlush(category);
    } catch (DataIntegrityViolationException exception) {
      throw new BusinessException(CategoryErrorCode.CONCURRENT_REQUEST_CONFLICT);
    }
  }

  /**
   * 카테고리 이름이 유효성 검증 조건을 충족하는지 확인한다.
   *
   * <p>null 또는 공백이거나, 앞뒤 공백이 포함되어 있거나, 최대 길이를 초과하면 거부한다.
   *
   * @param categoryName 검증할 카테고리 이름
   * @throws BusinessException 카테고리 이름이 유효하지 않은 경우
   */
  public static void validateCategoryName(String categoryName) {
    if (categoryName == null
        || categoryName.isBlank()
        || !categoryName.equals(categoryName.strip())
        || categoryName.length() > MAX_NAME_LENGTH) {
      throw new BusinessException(CategoryErrorCode.INVALID_CATEGORY_NAME);
    }
  }
}
