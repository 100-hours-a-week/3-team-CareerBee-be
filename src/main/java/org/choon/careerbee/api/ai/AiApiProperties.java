package org.choon.careerbee.api.ai;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AiApiProperties {

    @Value("${ai.api.base-url}")
    private String baseUrl;

}
