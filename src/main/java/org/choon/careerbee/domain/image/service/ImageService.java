package org.choon.careerbee.domain.image.service;

import org.choon.careerbee.domain.image.dto.request.PresignedUrlReq;
import org.choon.careerbee.domain.image.dto.response.PresignedUrlResp;

public interface ImageService {

    PresignedUrlResp generatePresignedUrl(PresignedUrlReq presignedUrlReq);

}
