package gaiden.da.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .headers(headers -> headers
                                .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable)
                                .contentTypeOptions(ServerHttpSecurity.HeaderSpec.ContentTypeOptionsSpec::disable)
                )
                .authorizeExchange(exchange -> exchange

                        .pathMatchers("/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .pathMatchers("/chat/**").permitAll()
                        .anyExchange().permitAll()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}