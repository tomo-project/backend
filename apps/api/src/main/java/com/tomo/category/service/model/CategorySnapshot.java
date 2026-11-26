package com.tomo.category.service.model;

/**
 * 카테고리 소유권 및 상태 확인용 스냅샷.
 *
 * @param categoryId 카테고리 ID
 * @param userId 카테고리 소유 사용자 ID
 * @param categoryName 카테고리 이름
 */
public record CategorySnapshot(Long categoryId, String userId, String categoryName) {}
