package com.tomo.category.controller.docs;

import com.tomo.category.dto.request.CategoryCreateRequest;
import com.tomo.category.dto.response.CategoryListResponse;
import com.tomo.common.openapi.OpenApiConfig;
import com.tomo.common.response.CommonResponse;
import com.tomo.common.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

/** 공부 카테고리 기능 명세. */
@Tag(name = "Category", description = "공부 카테고리 관리 기능")
public interface CategoryApiDocs {

  /**
   * 인증된 사용자의 공부 카테고리를 생성한다.
   *
   * @param userId 인증된 사용자 ID
   * @param requestBody 카테고리 생성 요청 본문
   * @param request HTTP 요청
   * @return 데이터가 없는 성공 응답
   */
  @Operation(
      summary = "카테고리 생성",
      description = "Access Token으로 인증된 사용자가 To-do에서 사용할 공부 카테고리를 생성합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "카테고리 생성 성공",
        useReturnTypeSchema = true,
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": true,
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "400",
        description = "카테고리 이름 유효성 검증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "INVALID_CATEGORY_NAME",
                                "message": "카테고리 이름이 형식 또는 유효성 검증 조건을 충족하지 않습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "401",
        description = "Access Token 인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "INVALID_ACCESS_TOKEN",
                                "message": "accessToken이 없거나 형식이 올바르지 않거나 만료·폐기되었습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "403",
        description = "카테고리 생성 권한 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "CATEGORY_CREATE_FORBIDDEN",
                                "message": "카테고리를 생성할 권한이 없습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "409",
        description = "동일한 카테고리 이름 중복 또는 동시 요청 충돌",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "CATEGORY_ALREADY_EXISTS",
                      summary = "동일한 이름의 카테고리가 이미 존재하는 경우",
                      value =
                          """
                          {
                            "success": false,
                            "error": {
                              "code": "CATEGORY_ALREADY_EXISTS",
                              "message": "동일한 이름의 카테고리가 이미 존재합니다."
                            },
                            "meta": {
                              "requestId": "req_01J9ABCDEF",
                              "timestamp": "2026-10-06T10:00:00+09:00"
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "CONCURRENT_REQUEST_CONFLICT",
                      summary = "카테고리 생성 요청이 동시에 처리되어 충돌한 경우",
                      value =
                          """
                          {
                            "success": false,
                            "error": {
                              "code": "CONCURRENT_REQUEST_CONFLICT",
                              "message": "카테고리 생성 요청이 동시에 처리되어 충돌이 발생했습니다."
                            },
                            "meta": {
                              "requestId": "req_01J9ABCDEF",
                              "timestamp": "2026-10-06T10:00:00+09:00"
                            }
                          }
                          """)
                })),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "INTERNAL_SERVER_ERROR",
                                "message": "서버 처리 중 오류가 발생했습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """)))
  })
  CommonResponse<Void> createCategory(
      @Parameter(hidden = true) String userId,
      CategoryCreateRequest requestBody,
      @Parameter(hidden = true) HttpServletRequest request);

  /**
   * 인증된 사용자의 공부 카테고리 목록을 조회한다.
   *
   * @param userId 인증된 사용자 ID
   * @param request HTTP 요청
   * @return 공부 카테고리 목록 응답
   */
  @Operation(
      summary = "카테고리 목록 조회",
      description = "Access Token으로 인증된 사용자 본인의 공부 카테고리 목록을 조회합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "카테고리 목록 조회 성공",
        useReturnTypeSchema = true,
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": true,
                              "data": {
                                "category": [
                                  {
                                    "categoryId": 1,
                                    "category_nm": "전공"
                                  }
                                ]
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "401",
        description = "Access Token 인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "INVALID_ACCESS_TOKEN",
                                "message": "accessToken이 없거나 형식이 올바르지 않거나 만료·폐기되었습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "403",
        description = "카테고리 목록 조회 권한 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "CATEGORY_VIEW_FORBIDDEN",
                                "message": "카테고리 목록을 조회할 권한이 없습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "success": false,
                              "error": {
                                "code": "INTERNAL_SERVER_ERROR",
                                "message": "서버 처리 중 오류가 발생했습니다."
                              },
                              "meta": {
                                "requestId": "req_01J9ABCDEF",
                                "timestamp": "2026-10-06T10:00:00+09:00"
                              }
                            }
                            """)))
  })
  CommonResponse<CategoryListResponse> getCategories(
      @Parameter(hidden = true) String userId,
      @Parameter(hidden = true) HttpServletRequest request);
}
