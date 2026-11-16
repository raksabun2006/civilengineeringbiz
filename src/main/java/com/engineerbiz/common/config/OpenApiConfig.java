package com.engineerbiz.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * OpenAPI 3.0 configuration with JWT Bearer Authentication scheme.
 * Accurately manages security locks:
 * - Public endpoints (login, refresh, activate, forgot-password, reset-password) have NO lock.
 * - All authenticated business endpoints have the lock icon and send Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    private static final Set<String> PUBLIC_POST_ENDPOINTS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/activate",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    );

    @Bean
    public OpenAPI engineerBizOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EngineerBiz - Civil Engineering Management API")
                        .description("REST API documentation for EngineerBiz internal management platform. Use the 'Authorize' button to authenticate with your Bearer JWT token.")
                        .version("v1")
                        .contact(new Contact()
                                .name("EngineerBiz Engineering Team")
                                .email("engineering@engineerbiz.com.kh"))
                        .license(new License().name("Proprietary - Internal Business Management")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token (Bearer <token>) to authenticate protected endpoints.")));
    }

    @Bean
    public OpenApiCustomizer openApiSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);

            openApi.getPaths().forEach((path, pathItem) -> {
                // Public authentication endpoints should NOT have a padlock / security requirement
                if (PUBLIC_POST_ENDPOINTS.contains(path)) {
                    if (pathItem.getPost() != null) {
                        pathItem.getPost().setSecurity(Collections.emptyList());
                    }
                    return;
                }

                // Public system / health endpoints should NOT have a padlock
                if (path.startsWith("/actuator") || path.startsWith("/error")) {
                    pathItem.readOperations().forEach(operation -> operation.setSecurity(Collections.emptyList()));
                    return;
                }

                // All other endpoints require authentication and MUST display the lock icon
                pathItem.readOperations().forEach(operation -> {
                    operation.setSecurity(List.of(securityRequirement));
                });
            });
        };
    }
}
