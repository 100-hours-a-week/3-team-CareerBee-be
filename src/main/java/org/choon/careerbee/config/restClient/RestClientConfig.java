package org.choon.careerbee.config.restClient;

import org.choon.careerbee.domain.company.api.SaraminApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient saraminRestClient(SaraminApiProperties props) {
        return RestClient.builder()
            .baseUrl(props.getBaseUrl())
            .build();
    }
}
