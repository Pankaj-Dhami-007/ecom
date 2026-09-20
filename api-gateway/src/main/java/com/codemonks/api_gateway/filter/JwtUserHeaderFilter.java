package com.codemonks.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


//This filter extracts user details from the validated JWT and forwards them
// as HTTP headers to downstream microservices.
@Component
@Slf4j
public class JwtUserHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // Get the authenticated user's Security Context
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {

                        // Read claims from JWT
                        String userId = jwt.getClaimAsString("userId");
                        String email = jwt.getClaimAsString("sub");
                        List<String> roles = jwt.getClaimAsStringList("roles");
                        String roleHeader = roles == null
                                ? ""
                                : String.join(",", roles);
                        log.debug("Forwarding JWT claims - userId: {}, email: {}", userId, email);

                        // Add user information as request headers
                        // so downstream microservices can access it
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", userId != null ? userId : "")
                                .header("X-User-Email", email != null ? email : "")
                                .header("X-User-Roles", roleHeader)
                                .build();

                        // Continue filter chain with modified request
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                    // Continue request if no JWT is found
                    return chain.filter(exchange);
                })
                // Continue request if Security Context is empty
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Filter execution priority.
        // Lower value = higher priority.
        return 0;
    }
}

// ServerWebExchange -> Contains request + response.
// MVC equivalent: HttpServletRequest, HttpServletResponse

// GatewayFilterChain  -> for next filter
// if work done then chain.filter(exchange)

// asynchronous operation
// Mono<Void> = reactive version of void

// GlobalFilter -> run for every request
// Ordered -> defines filter execution priority
// ServerWebExchange -> request + response container
// GatewayFilterChain -> passes request to next filter
// Mono<Void> -> reactive completion signal



/*

flow

Client
  ↓
JWT Token
  ↓
Gateway

oauth2ResourceServer()
  ↓
JWT Validated
  ↓
JwtUserHeaderFilter
  ↓
Extract:
- userId
- email
- roles
  ↓
Add Headers:
X-User-Id
X-User-Email
X-User-Roles
  ↓
Forward Request
  ↓
Product Service / Order Service
 */

/*

OncePerRequestFilter   and  GlobalFilter

OncePerRequestFilter
use in : Spring MVC , Spring Boot Servlet Stack
uses -> HttpServletRequest, HttpServletResponse, Servlet API, Blocking

GlobalFilter
use in : Spring Cloud Gateway, WebFlux
uses -> ServerWebExchange, GatewayFilterChain, Reactive, Non-blocking


Main thread is mainly responsible for starting the Spring application and server.
After startup, incoming requests are handled by Tomcat worker threads (MVC) or
Netty event-loop threads (WebFlux), not by the main thread.
 */
