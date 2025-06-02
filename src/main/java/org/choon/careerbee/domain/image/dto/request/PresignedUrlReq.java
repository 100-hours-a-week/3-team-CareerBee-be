package org.choon.careerbee.domain.image.dto.request;

import org.choon.careerbee.domain.image.enums.SupportedExtension;
import org.choon.careerbee.domain.image.enums.UploadType;

public record PresignedUrlReq(
    String fileName,
    SupportedExtension extension,
    UploadType uploadType
) {

}
