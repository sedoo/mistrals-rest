package fr.sedoo.mistrals.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;


@OpenAPIDefinition(servers = { @Server(url = "${springdoc.server.url}")})
@SecurityScheme(name = "authentication", type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode = @OAuthFlow( authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}", tokenUrl = "${springdoc.oAuthFlow.tokenUrl}")))
public class OpenApi3Config {
}

