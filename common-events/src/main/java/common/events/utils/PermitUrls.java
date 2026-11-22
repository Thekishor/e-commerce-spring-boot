package common.events.utils;

public class PermitUrls {

    private PermitUrls(){}

    public static final String[] SERVICE_URLS = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh-token",
            "/api/user/activate",
            "/api/user/reset-password",
            "/api/user/save-password",
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
