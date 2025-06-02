package org.choon.careerbee.domain.image.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.common.dto.CommonResponse;
import org.choon.careerbee.common.dto.CommonResponseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.dto.response.PresignedUrlResp;
import org.choon.careerbee.domain.image.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class ImageController {

    private final ImageService imageService;

    @PostMapping("s3/presigned-url")
    public ResponseEntity<CommonResponse<PresignedUrlResp>> getPresignedUrl(
        @RequestBody PresignedUrlReq presignedUrlReq
    ) {
        PresignedUrlResp response = imageService.generatePresignedUrl(presignedUrlReq);

        return CommonResponseEntity.ok(
            response,
            CustomResponseStatus.SUCCESS,
            "presignedUrl 생성에 성공하였습니다."
        );
    }
}
