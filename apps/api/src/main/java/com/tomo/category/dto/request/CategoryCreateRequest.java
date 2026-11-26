package com.tomo.category.dto.request;

import com.tomo.category.service.command.CreateCategoryCommand;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 카테고리 생성 요청 DTO.
 *
 * @param category 생성할 공부 카테고리 이름
 */
@Schema(description = "카테고리 생성 요청")
public record CategoryCreateRequest(
    @Schema(
            description = "공부 카테고리 (앞뒤 공백 불가, 1~100자)",
            example = "전공",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String category) {

  /**
   * 요청 DTO를 카테고리 생성 Command로 변환한다.
   *
   * @param userId 인증된 사용자 ID
   * @return 카테고리 생성 Command
   */
  public CreateCategoryCommand toCommand(String userId) {
    return new CreateCategoryCommand(userId, category);
  }
}
