package org.choon.careerbee.domain.image.service;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.dto.response.GetPresignedUrlResp;
import org.choon.careerbee.domain.image.dto.response.ObjectUrlResp;
import org.choon.careerbee.domain.image.dto.response.PresignedUrlResp;
import org.choon.careerbee.domain.image.enums.SupportedExtension;
import org.choon.careerbee.domain.image.enums.UploadType;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
@Slf4j
@Service
public class S3Service implements ImageService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(15);

    private static final String PROFILE_IMAGE_BASE_PATH = "profile_images/";
    private static final String RESUME_BASE_PATH = "user_resume/";

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.image-bucket}")
    private String imageBucket;

    @Value("${spring.cloud.aws.s3.resume-bucket}")
    private String resumeBucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Override
    public PresignedUrlResp generatePresignedUrl(PresignedUrlReq request) {
        SupportedExtension extension = request.extension();
        UploadType uploadType = request.uploadType();

        validateUploadTypeAndExtension(uploadType, extension);

        String basePath = switch (uploadType) {
            case PROFILE_IMAGE -> PROFILE_IMAGE_BASE_PATH;
            case RESUME -> RESUME_BASE_PATH;
        };

        String targetBucket = switch (uploadType) {
            case PROFILE_IMAGE -> imageBucket;
            case RESUME -> resumeBucket;
        };

        String key = basePath + UUID.randomUUID() + "." + extension.getExt();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(targetBucket)
            .key(key)
            .contentType(extension.getMimeType())
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(PRESIGNED_URL_EXPIRATION)
            .build();

        URL uploadUrl = s3Presigner.presignPutObject(presignRequest).url();

        return new PresignedUrlResp(uploadUrl.toString(), key);
    }

    @Override
    public GetPresignedUrlResp generateGetPresignedUrlByObjectKey(
        UploadCompleteReq uploadCompleteReq
    ) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(resumeBucket)
            .key(uploadCompleteReq.objectKey())
            .build();

        GetObjectPresignRequest getPresignRequest = GetObjectPresignRequest.builder()
            .getObjectRequest(getObjectRequest)
            .signatureDuration(PRESIGNED_URL_EXPIRATION)
            .build();

        URL getUrl = s3Presigner.presignGetObject(getPresignRequest).url();

        return new GetPresignedUrlResp(
            getUrl.toString()
        );
    }

    @Override
    public ObjectUrlResp getObjectUrlByKey(String objectKey) {
        String objectUrl =
            "https://" + imageBucket + ".s3." + region + ".amazonaws.com/" + objectKey;
        return new ObjectUrlResp(objectUrl);
    }

    private void validateUploadTypeAndExtension(UploadType type, SupportedExtension ext) {
        if ((type == UploadType.RESUME && ext != SupportedExtension.PDF)
            ||
            (type == UploadType.PROFILE_IMAGE && ext == SupportedExtension.PDF)
        ) {
            throw new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST);
        }
    }
}
