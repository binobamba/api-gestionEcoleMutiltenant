package com.edusecure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API GESTION D'ECOLE ")
                        .description("""
                                API multi-tenant — isolation forte par base de données.
                                Chaque requête doit inclure le header X-Tenant-ID.
                                """)
                        .version("1.0.0")
                        )

                // Ajoute le bouton "Authorize" avec JWT dans Swagger UI
                .addSecurityItem( new SecurityRequirement().addList("Bearer Authentication")).components(new Components().addSecuritySchemes("Bearer Authentication",new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenu via /auth/login")));
    }

    // Ajoute automatiquement X-Tenant-ID sur TOUS les endpoints dans Swagger
    @Bean
    public GlobalOpenApiCustomizer tenantHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths()
                    .values()
                    .forEach(pathItem ->
                            pathItem.readOperations().forEach(operation ->
                                    operation.addParametersItem(
                                            new Parameter()
                                                    .in("header")
                                                    .name("X-Tenant-ID")
                                                    .description("Identifiant de l'école (ex: lycee-hugo)")
                                                    .required(false)
                                                    .schema(new StringSchema().example("lycee-victor-hugo"))
                                    )
                            )
                    );
        };
    }
}