package com.engineerbiz.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("Should configure OpenAPI with BearerAuth security scheme")
    void testOpenApiConfiguration() {
        OpenAPI openAPI = openApiConfig.engineerBizOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);
        assertThat(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME).getType().toString())
                .isEqualToIgnoringCase("HTTP");
        assertThat(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME).getScheme())
                .isEqualToIgnoringCase("bearer");
        assertThat(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME).getBearerFormat())
                .isEqualTo("JWT");
    }

    @Test
    @DisplayName("Security Customizer should remove lock from public endpoints and add lock to secured endpoints")
    void testOpenApiSecurityCustomizer() {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        // 1. Public endpoint (POST /api/v1/auth/login)
        PathItem loginPathItem = new PathItem();
        Operation loginOp = new Operation().summary("Login");
        loginPathItem.setPost(loginOp);
        paths.addPathItem("/api/v1/auth/login", loginPathItem);

        // 2. Public endpoint (POST /api/v1/auth/activate)
        PathItem activatePathItem = new PathItem();
        Operation activateOp = new Operation().summary("Activate");
        activatePathItem.setPost(activateOp);
        paths.addPathItem("/api/v1/auth/activate", activatePathItem);

        // 3. Authenticated auth endpoint (POST /api/v1/auth/logout)
        PathItem logoutPathItem = new PathItem();
        Operation logoutOp = new Operation().summary("Logout");
        logoutPathItem.setPost(logoutOp);
        paths.addPathItem("/api/v1/auth/logout", logoutPathItem);

        // 4. Authenticated profile endpoint (GET /api/v1/auth/me)
        PathItem mePathItem = new PathItem();
        Operation meOp = new Operation().summary("Me");
        mePathItem.setGet(meOp);
        paths.addPathItem("/api/v1/auth/me", mePathItem);

        // 5. Business endpoint (GET /api/v1/estimates/{id})
        PathItem estimatePathItem = new PathItem();
        Operation getEstimateOp = new Operation().summary("Get Estimate");
        estimatePathItem.setGet(getEstimateOp);
        paths.addPathItem("/api/v1/estimates/{id}", estimatePathItem);

        // 6. Business endpoint (POST /api/v1/users)
        PathItem userPathItem = new PathItem();
        Operation createUserOp = new Operation().summary("Create User");
        userPathItem.setPost(createUserOp);
        paths.addPathItem("/api/v1/users", userPathItem);

        openAPI.setPaths(paths);

        OpenApiCustomizer customizer = openApiConfig.openApiSecurityCustomizer();
        customizer.customise(openAPI);

        // Public endpoints must have empty security (NO LOCK in Swagger UI)
        assertThat(loginOp.getSecurity()).isEmpty();
        assertThat(activateOp.getSecurity()).isEmpty();

        // Protected endpoints must have BearerAuth security (LOCK ICON in Swagger UI)
        assertThat(logoutOp.getSecurity()).isNotEmpty();
        assertThat(logoutOp.getSecurity().get(0)).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);

        assertThat(meOp.getSecurity()).isNotEmpty();
        assertThat(meOp.getSecurity().get(0)).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);

        assertThat(getEstimateOp.getSecurity()).isNotEmpty();
        assertThat(getEstimateOp.getSecurity().get(0)).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);

        assertThat(createUserOp.getSecurity()).isNotEmpty();
        assertThat(createUserOp.getSecurity().get(0)).containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);
    }
}
