package com.tomo.category.service;

import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.service.command.CreateCategoryCommand;
import com.tomo.common.auth.exception.AuthErrorCode;
import com.tomo.common.exception.BusinessException;
import com.tomo.user.service.UserCategoryQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사용자 권한 확인과 카테고리 업무 서비스를 조정한다. */
@Service
public class CategoryFacade {
  private final UserCategoryQueryService userCategoryQueryService;
  private final CategoryCommandService categoryCommandService;

  /**
   * 사용자 활성 상태 조회 계약과 카테고리 Command 서비스를 주입받는다.
   *
   * @param userCategoryQueryService 사용자 활성 상태 조회 서비스
   * @param categoryCommandService 카테고리 생성·변경 서비스
   */
  public CategoryFacade(
      UserCategoryQueryService userCategoryQueryService,
      CategoryCommandService categoryCommandService) {
    this.userCategoryQueryService = userCategoryQueryService;
    this.categoryCommandService = categoryCommandService;
  }

  /**
   * 인증된 사용자의 공부 카테고리를 생성한다.
   *
   * @param command 카테고리 생성 입력 값
   * @throws BusinessException 인증 정보가 없거나, 카테고리 이름이 유효하지 않거나, 생성 권한이 없거나, 중복·동시 요청 충돌이 발생한 경우
   */
  @Transactional
  public void createCategory(CreateCategoryCommand command) {
    if (command == null || command.userId() == null || command.userId().isBlank()) {
      throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    CategoryCommandService.validateCategoryName(command.categoryName());

    if (!userCategoryQueryService.isActiveCategoryUser(command.userId())) {
      throw new BusinessException(CategoryErrorCode.CATEGORY_CREATE_FORBIDDEN);
    }

    categoryCommandService.createCategory(command.userId(), command.categoryName());
  }
}
