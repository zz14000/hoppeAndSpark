package com.hopeandsparks.boot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * 创建 Swagger/OpenAPI 文档配置。
     * 文档中注册 bearerAuth 安全方案，方便在 Swagger UI 里填入 JWT 后直接测试受保护接口。
     */
    @Bean
    public OpenAPI hopeAndSparksOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hope and Sparks API")
                        .version("0.1.0")
                        .description("Spark 前台与 Manage 后台接口在线测试文档"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
