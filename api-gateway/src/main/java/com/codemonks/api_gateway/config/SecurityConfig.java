package com.codemonks.api_gateway.config;

import com.codemonks.api_gateway.constants.ApiPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(ApiPaths.AUTH_REGISTER).permitAll()
                        .pathMatchers(ApiPaths.AUTH_LOGIN).permitAll()
                        .pathMatchers(ApiPaths.AUTH_FORGOT_PASSWORD).permitAll()
                        .pathMatchers(ApiPaths.AUTH_RESET_PASSWORD).permitAll()

                        .pathMatchers(ApiPaths.PRODUCTS_PATTERN).authenticated()
                        .pathMatchers(ApiPaths.CATEGORIES_PATTERN).authenticated()
                        .pathMatchers(ApiPaths.ORDERS_PATTERN).authenticated()
                        .pathMatchers(ApiPaths.CARTS_PATTERN).authenticated()

                        .pathMatchers(ApiPaths.ACTUATOR_HEALTH).permitAll()

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults())
                )
                .build();
    }
}
