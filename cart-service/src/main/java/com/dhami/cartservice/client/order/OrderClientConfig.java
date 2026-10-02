package com.dhami.cartservice.client.order;

import com.dhami.cartservice.common.context.RequestUserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderClientConfig {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Bean
    public RequestInterceptor userContextRequestInterceptor() {
        return template -> {
            Long userId = RequestUserContext.getUserId();
            if (userId != null) {
                template.header(USER_ID_HEADER, userId.toString());
            }
        };
    }
}
