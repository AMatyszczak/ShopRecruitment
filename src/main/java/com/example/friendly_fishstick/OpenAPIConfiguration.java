package com.example.friendly_fishstick;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "basicAuth", scheme = "basic", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER, description = """
        There are two predefined users with different resource entitlements based on challenge description.
        
        Credentials for:
        
           admin: {admin:password}
        
           customer: {customer:password}""")
@OpenAPIDefinition(
        info = @Info(title = "Orders API", version = "v1", description = "Make sure to authorize by clicking 'Authorize' button on right"),
        security = @SecurityRequirement(name = "basicAuth")
)
public class OpenAPIConfiguration {
}
