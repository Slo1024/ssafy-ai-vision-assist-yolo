package com.project.lookey.config;

import com.project.lookey.OAuth.Service.jwt.JwtAuthenticationFilter;
import com.project.lookey.OAuth.Service.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // CORS Preflight 요청은 항상 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 API (인증 불필요)
                        .requestMatchers("/api/auth/**").permitAll() // 로그인/인증 관련
                        .requestMatchers("/api/test/**").permitAll() // 테스트 API
                        .requestMatchers("/api/v1/allergy/search/**").permitAll() // 알레르기 검색
                        .requestMatchers("/api/product/seven/drinks").permitAll() // product db API
                        .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger

                        // 인증 필요 API
                        .requestMatchers("/api/v1/allergy").authenticated() // 내 알레르기 목록, 추가, 삭제
                        .requestMatchers("/api/v1/carts/**").authenticated() // 장바구니 관련
                        .requestMatchers("/api/v1/product/**").authenticated() // 상품 관련 (검색 제외)
                        .requestMatchers("/api/v1/vision/**").authenticated() // Vision API - 인증 필요

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setHeader("Access-Control-Allow-Origin", "*");
                            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                            response.setHeader("Access-Control-Allow-Headers", "*");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setHeader("Access-Control-Allow-Origin", "*");
                            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                            response.setHeader("Access-Control-Allow-Headers", "*");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://j13e101.p.ssafy.io",
                "http://j13e101.p.ssafy.io:8081",
                "http://j13e101.p.ssafy.io:8082",
                "http://localhost:8080",
                "http://localhost:8081",
                "http://localhost:8082"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}