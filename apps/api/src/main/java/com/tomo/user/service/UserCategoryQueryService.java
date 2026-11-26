package com.tomo.user.service;

/** 카테고리 도메인에서 사용자 활성 상태를 확인하기 위한 공개 조회 계약. */
public interface UserCategoryQueryService {

  /**
   * 카테고리 기능을 요청한 사용자가 활성 계정인지 확인한다.
   *
   * @param userId 인증된 사용자 ID
   * @return 활성 상태의 계정이면 true
   */
  boolean isActiveCategoryUser(String userId);
}
