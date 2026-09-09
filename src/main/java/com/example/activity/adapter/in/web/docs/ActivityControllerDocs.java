package com.example.activity.adapter.in.web.docs;

import jakarta.validation.constraints.Positive;

import com.example.activity.adapter.in.web.ActivityDescriptionResponse;
import com.example.activity.application.error.ActivityErrorCode;
import com.example.shared.error.CommonErrorCode;
import com.example.shared.openapi.ApiErrorCodes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Activities", description = "사용자 활동 API")
public interface ActivityControllerDocs {

    @Operation(summary = "사용자 활동 설명 조회", description = "사용자 식별자로 활동 설명을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActivityDescriptionResponse.class)))
    @ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
    @ApiErrorCodes(enumClass = ActivityErrorCode.class, includes = "USER_NOT_FOUND")
    ActivityDescriptionResponse describe(
            @Parameter(description = "사용자 식별자", example = "1", required = true) @Positive Long userId);
}
