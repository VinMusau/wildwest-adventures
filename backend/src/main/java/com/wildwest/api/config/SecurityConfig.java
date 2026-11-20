package com.wildwest.api.config;

import com.wildwest.api.auth.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // No token, or an invalid/expired one: 401 so the frontend can send the user to log in.
                        .authenticationEntryPoint((request, response, e) -> writeProblem(
                                response, HttpStatus.UNAUTHORIZED, "Authentication required. Please log in again."))
                        // Logged in but not allowed (e.g. a non-admin on /admin).
                        .accessDeniedHandler((request, response, e) ->
                                writeProblem(response, HttpStatus.FORBIDDEN, "You don't have permission to do that.")))
                .authorizeHttpRequests(auth -> auth
                        // Spring Boot's error page. Without this, any error that reaches it is
                        // replaced by an empty 403, hiding the real status and message.
                        .requestMatchers("/error")
                        .permitAll()

                        // Public: health
                        .requestMatchers("/api/v1/health", "/actuator/health")
                        .permitAll()

                        // Public: auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
                        .permitAll()

                        // Public: read-only content
                        .requestMatchers(HttpMethod.GET, "/api/v1/tours/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/travel-styles/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/webhooks/**")
                        .permitAll()

                        // Admin-only
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeProblem(HttpServletResponse response, HttpStatus status, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        // detail is always one of the constant messages above, so no JSON escaping is needed.
        response.getWriter()
                .write("{\"type\":\"about:blank\",\"title\":\"" + status.getReasonPhrase()
                        + "\",\"status\":" + status.value()
                        + ",\"detail\":\"" + detail + "\"}");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
