package gaiden.da.apigateway.filters;

import gaiden.da.apigateway.utils.JwtUtils;
import gaiden.da.apigateway.utils.RouteValidator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtils jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtils jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {


            if (validator.isSecured.test(exchange.getRequest())) {


                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Відсутній заголовок авторизації", HttpStatus.UNAUTHORIZED);
                }


                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }


                try {
                    jwtUtil.validateToken(authHeader);

                    String username = jwtUtil.extractUsername(authHeader);
                    Long userId = jwtUtil.extractUserId(authHeader);


                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header("X-User-Name", username)
                            .header("X-User-Id", String.valueOf(userId))
                            .build();


                    return chain.filter(exchange.mutate().request(request).build());

                } catch (Exception e) {
                    System.out.println("Невалідний доступ...!");
                    return onError(exchange, "Невалідний токен", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        });
    }


    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}
