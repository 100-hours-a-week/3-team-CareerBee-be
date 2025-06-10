package org.choon.careerbee.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URL;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.dto.response.PresignedUrlResp;
import org.choon.careerbee.domain.image.enums.SupportedExtension;
import org.choon.careerbee.domain.image.enums.UploadType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest mockPresignedRequest;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Presigner);
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("프로필 이미지 presigned URL 생성 성공")
    void generatePresignedUrl_profileImage_success() {
        // given
        PresignedUrlReq req = new PresignedUrlReq(
            "test.jpg", SupportedExtension.JPG, UploadType.PROFILE_IMAGE
        );

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
            .thenReturn(mockPresignedRequest);
        when(mockPresignedRequest.url())
            .thenReturn(newURL("https://test-bucket.s3.amazonaws.com/profile_images/test.jpg"));

        // when
        PresignedUrlResp response = s3Service.generatePresignedUrl(req);

        // then
        assertThat(response.uploadUrl()).contains("https://test-bucket.s3.amazonaws.com/");
        assertThat(response.objectKey()).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("이력서 업로드에 PDF 외 확장자 요청 시 예외 발생")
    void generatePresignedUrl_resumeWithNonPdf_throwsException() {
        PresignedUrlReq req = new PresignedUrlReq("resume.jpg", SupportedExtension.JPG,
            UploadType.RESUME);

        assertThatThrownBy(() -> s3Service.generatePresignedUrl(req))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.EXTENSION_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("프로필 이미지 업로드에 PDF 확장자 요청 시 예외 발생")
    void generatePresignedUrl_profileImageWithPdf_throwsException() {
        PresignedUrlReq req = new PresignedUrlReq("profile.pdf", SupportedExtension.PDF,
            UploadType.PROFILE_IMAGE);

        assertThatThrownBy(() -> s3Service.generatePresignedUrl(req))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(CustomResponseStatus.EXTENSION_NOT_EXIST.getMessage());
    }

    private URL newURL(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
