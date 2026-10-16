package org.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Global OpenAPI / Swagger configuration.
 *
 * - Registers the "bearerAuth" JWT security scheme.
 * - Applies it globally so every protected endpoint shows the padlock in Swagger UI.
 * - Public endpoints override with no security requirement in their own @Operation.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "Yahweh Bible App API",
                version     = "1.0.0",
                description = """
                        REST API for the Yahweh Bible App.

                        **Authentication**
                        Most endpoints require a Bearer JWT in the `Authorization` header.
                        Obtain tokens via `POST /api/auth/login`, `POST /api/auth/register`,
                        or `POST /api/auth/google`.
                        Use `POST /api/auth/refresh` to exchange an expiring access token.

                        **Token lifetimes**
                        - Access token: 15 minutes (configurable)
                        - Refresh token: 30 days (configurable)
                        """,
                contact = @Contact(
                        name  = "Maurice Ombewa",
                        email = "mauriceombewa@protonmail.com"
                ),
                license = @License(
                        name = "MIT",
                        url  = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "/", description = "Current server"),
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name         = "bearerAuth",
        type         = SecuritySchemeType.HTTP,
        scheme       = "bearer",
        bearerFormat = "JWT",
        description  = "Paste your access token here (without the 'Bearer ' prefix)."
)
public class OpenApiConfig {
    // All configuration is expressed through annotations above.
}
