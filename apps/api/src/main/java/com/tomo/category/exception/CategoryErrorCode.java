package com.tomo.category.exception;

import com.tomo.common.exception.ErrorCode;

/** 카테고리 기능에서 사용하는 오류 코드. */
public enum CategoryErrorCode implements ErrorCode {
  /** 카테고리 이름이 형식 또는 유효성 검증 조건을 충족하지 않습니다. */
  INVALID_CATEGORY_NAME(400, "카테고리 이름이 형식 또는 유효성 검증 조건을 충족하지 않습니다."),

  /** 카테고리를 생성할 권한이 없습니다. */
  CATEGORY_CREATE_FORBIDDEN(403, "카테고리를 생성할 권한이 없습니다."),

  /** 동일한 이름의 카테고리가 이미 존재합니다. */
  CATEGORY_ALREADY_EXISTS(409, "동일한 이름의 카테고리가 이미 존재합니다."),

  /** 카테고리 생성 요청이 동시에 처리되어 충돌이 발생했습니다. */
  CONCURRENT_REQUEST_CONFLICT(409, "카테고리 생성 요청이 동시에 처리되어 충돌이 발생했습니다.");

  private final int httpStatus;
  private final String message;

  /**
   * 오류의 HTTP 상태 코드와 메시지를 설정한다.
   *
   * @param httpStatus HTTP 상태 코드
   * @param message 클라이언트에 전달할 오류 메시지
   */
  CategoryErrorCode(int httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }

  /**
   * 오류 코드 이름을 반환한다.
   *
   * @return 오류 코드 이름
   */
  @Override
  public String code() {
    return name();
  }

  /**
   * 오류 메시지를 반환한다.
   *
   * @return 클라이언트에 전달할 오류 메시지
   */
  @Override
  public String message() {
    return message;
  }

  /**
   * 오류의 HTTP 상태 코드를 반환한다.
   *
   * @return HTTP 상태 코드
   */
  @Override
  public int httpStatus() {
    return httpStatus;
  }
}
