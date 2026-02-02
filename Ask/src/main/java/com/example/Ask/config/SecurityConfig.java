package com.example.Ask.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // --- 1. Define CORS Configuration Correctly ---
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // FIX: Allow ONLY your frontend URL. Wildcards (*) fail with Auth headers.
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:8083")); 
        
        // Allow all methods (GET, POST, PUT, DELETE, OPTIONS)
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers (Authorization, Content-Type, etc.)
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        
        // FIX: Allow credentials (cookies/authorization headers)
        corsConfiguration.setAllowCredentials(true);

        // --- 2. Apply Configuration ---
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> corsConfiguration))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow public access to login/register
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll()
                
                // Allow public access to basic endpoints
                .requestMatchers(HttpMethod.GET, "/", "/test", "/api", "/api/test", "/api/health", "/error").permitAll()
                
                // FIX: Allow public access to IMAGES (GET only is safer, but AntPath works too)
                .requestMatchers("/api/files/image/**").permitAll()
                
                // Explicitly permit OPTIONS requests globally (fixes some pre-flight 401s)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Protected endpoints
                .requestMatchers(HttpMethod.PUT, "/api/animals/Deny/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/user/role/add/**").authenticated()
                
                // Swagger/Actuator
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v2/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

