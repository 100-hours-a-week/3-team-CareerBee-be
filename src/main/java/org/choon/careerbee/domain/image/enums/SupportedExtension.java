package org.choon.careerbee.domain.image.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.Getter;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;

@Getter
public enum SupportedExtension {
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp"),
    HEIC("heic", "image/heic"),
    PDF("pdf", "application/pdf");

    private final String ext;
    private final String mimeType;

    SupportedExtension(String ext, String mimeType) {
        this.ext = ext;
        this.mimeType = mimeType;
    }

    @JsonCreator
    public static SupportedExtension from(String value) {
        return Arrays.stream(SupportedExtension.values())
            .filter(e -> e.getExt().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new CustomException(CustomResponseStatus.EXTENSION_NOT_EXIST));
    }

}
