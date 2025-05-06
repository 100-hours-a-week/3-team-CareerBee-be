package org.choon.careerbee.domain.company.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SaraminApiProperties {
    @Value("${saramin.api.base-url}")
    private String baseUrl;

    @Value("${saramin.api.accessKey}")
    private String accessKey;

}
