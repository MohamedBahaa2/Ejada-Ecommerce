package com.ejada.ecommerce.wallet.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;


@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        String rawUserId = webRequest.getHeader(GatewayHeaders.USER_ID);
        if (rawUserId == null || rawUserId.isBlank()) {
            throw new MissingIdentityHeaderException(
                    "Missing " + GatewayHeaders.USER_ID + " header. "
                            + "This request did not pass through the API gateway.");
        }

        String userId = rawUserId.trim();

        String rawRoles = webRequest.getHeader(GatewayHeaders.USER_ROLES);
        List<String> roles = (rawRoles == null || rawRoles.isBlank())
                ? List.of()
                : Arrays.stream(rawRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return new AuthenticatedUser(userId, roles);
    }
}