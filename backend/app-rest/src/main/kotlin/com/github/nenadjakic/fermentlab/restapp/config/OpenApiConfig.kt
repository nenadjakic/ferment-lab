package com.github.nenadjakic.fermentlab.restapp.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("FermentLab API")
                .version("1.0")
                .description(
                    """
                    Fermentation journal for home fermenters: batch tracking, scientific charts,
                    side by side batch comparison, a culture library with family trees, a recipe
                    library and over-fermentation detection.
                    """.trimIndent()
                )
                .license(License().name("MIT"))
        )
}
