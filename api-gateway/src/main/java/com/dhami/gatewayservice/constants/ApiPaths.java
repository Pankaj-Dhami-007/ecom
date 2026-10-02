package com.dhami.gatewayservice.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String AUTH_REGISTER = "/auth/api/v1/register";
    public static final String AUTH_LOGIN = "/auth/api/v1/login";
    public static final String AUTH_FORGOT_PASSWORD = "/auth/api/v1/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/auth/api/v1/reset-password";

    public static final String PRODUCTS_PATTERN = "/api/v1/products/**";
    public static final String CATEGORIES_PATTERN = "/api/v1/categories/**";
    public static final String ORDERS_PATTERN = "/api/v1/orders/**";
    public static final String CARTS_PATTERN = "/api/v1/carts/**";

    public static final String ACTUATOR_HEALTH = "/actuator/health";
}
