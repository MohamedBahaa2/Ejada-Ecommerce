package com.ejada.ecommerce.shop.config;

import com.ejada.ecommerce.shop.security.GatewayHeaders;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableFeignClients(basePackages = "com.ejada.ecommerce.shop.client")
public class FeignConfig {

    @Bean
    public RequestInterceptor identityForwardingInterceptor() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                var request = servletAttrs.getRequest();
                String userId = request.getHeader(GatewayHeaders.USER_ID);
                String roles = request.getHeader(GatewayHeaders.USER_ROLES);
                if (userId != null) {
                    template.header(GatewayHeaders.USER_ID, userId);
                }
                if (roles != null) {
                    template.header(GatewayHeaders.USER_ROLES, roles);
                }
            }
        };
    }
}