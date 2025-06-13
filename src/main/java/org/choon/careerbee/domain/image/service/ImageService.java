package org.choon.careerbee.domain.image.service;

import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.dto.response.GetPresignedUrlResp;
import org.choon.careerbee.domain.image.dto.response.ObjectUrlResp;
import org.choon.careerbee.domain.image.dto.response.PresignedUrlResp;
import org.choon.careerbee.domain.member.dto.request.UploadCompleteReq;

public interface ImageService {

    PresignedUrlResp generatePresignedUrl(PresignedUrlReq presignedUrlReq);

    GetPresignedUrlResp generateGetPresignedUrlByObjectKey(UploadCompleteReq uploadCompleteReq);

    ObjectUrlResp getObjectUrlByKey(String objectKey);
}
