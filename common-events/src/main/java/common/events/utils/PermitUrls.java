package common.events.utils;

public class PermitUrls {

    public static final String[] SERVICE_URLS = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh",
            "/api/user/activate",
            "/api/user/resetPassword",
            "/api/user/savePassword",
            "/actuator/**"
    };

    public static final String[] OPENAPI_URLS = {
            "/api/v1/auth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };
}
