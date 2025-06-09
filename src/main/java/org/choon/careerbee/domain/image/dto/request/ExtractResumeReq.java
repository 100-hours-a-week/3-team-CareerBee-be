package org.choon.careerbee.domain.image.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExtractResumeReq(
    @JsonProperty("file_url") String fileUrl
) {

}
