package com.tomo.category.controller;

import com.tomo.category.controller.docs.CategoryApiDocs;
import com.tomo.category.dto.request.CategoryCreateRequest;
import com.tomo.category.dto.response.CategoryListResponse;
import com.tomo.category.exception.CategoryErrorCode;
import com.tomo.category.service.CategoryCommandService;
import com.tomo.category.service.CategoryFacade;
import com.tomo.common.exception.BusinessException;
import com.tomo.common.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 공부 카테고리 기능을 처리한다. */
@RestController
@RequestMapping("/api/v1/category")
public class CategoryController implements CategoryApiDocs {
  private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);

  private final CategoryFacade categoryFacade;

  /**
   * 카테고리 Facade를 주입받는다.
   *
   * @param categoryFacade 카테고리 업무 조정 객체
   */
  public CategoryController(CategoryFacade categoryFacade) {
    this.categoryFacade = categoryFacade;
  }

  /**
   * 현재 인증된 사용자의 공부 카테고리를 생성한다.
   *
   * @param userId 인증된 사용자 ID
   * @param requestBody 카테고리 생성 요청 본문
   * @param request HTTP 요청
   * @return 데이터가 없는 성공 응답
   * @throws BusinessException 요청 본문이나 카테고리 이름이 유효하지 않거나 생성할 수 없는 경우
   */
  @Override
  @PostMapping
  public CommonResponse<Void> createCategory(
      @AuthenticationPrincipal String userId,
      @RequestBody(required = false) CategoryCreateRequest requestBody,
      HttpServletRequest request) {
    if (requestBody == null) {
      throw new BusinessException(CategoryErrorCode.INVALID_CATEGORY_NAME);
    }

    CategoryCommandService.validateCategoryName(requestBody.categoryName());

    categoryFacade.createCategory(requestBody.toCommand(userId));

    LOG.debug("카테고리 생성 완료");

    return CommonResponse.successWithoutData(request.getRequestId());
  }

  /**
   * 현재 인증된 사용자의 공부 카테고리 목록을 조회한다.
   *
   * @param userId 인증된 사용자 ID
   * @param request HTTP 요청
   * @return 공부 카테고리 목록 응답
   */
  @Override
  @GetMapping
  public CommonResponse<CategoryListResponse> getCategories(
      @AuthenticationPrincipal String userId, HttpServletRequest request) {
    CategoryListResponse response = CategoryListResponse.from(categoryFacade.getCategories(userId));

    LOG.debug("카테고리 목록 조회 완료");

    return CommonResponse.success(response, request.getRequestId());
  }
}
