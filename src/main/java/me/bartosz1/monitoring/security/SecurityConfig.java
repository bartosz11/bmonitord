package me.bartosz1.monitoring.security;

import me.bartosz1.monitoring.ActualOriginCookieFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final ActualOriginCookieFilter actualOriginCookieFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, ActualOriginCookieFilter actualOriginCookieFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.actualOriginCookieFilter = actualOriginCookieFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable).sessionManagement(sessionMgmt -> sessionMgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        //frontend requests
                        .requestMatchers("/", "/index.html").permitAll()
                        //some routes that may be utilized by client side routers on frontend
                        .requestMatchers("/dashboard/**", "/auth/**", "/report/**", "/statuspage/**").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        //all login and register requests need to be allowed obviously
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        //allow all requests agent make - post data, install signal etc
                        .requestMatchers("/api/agent/**").permitAll()
                        //public data
                        .requestMatchers("/api/heartbeat/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/monitor/{id}").permitAll()
                        .requestMatchers("/api/monitor/{monitorId}/agent").permitAll()
                        .requestMatchers("/api/incident/**").permitAll()
                        .requestMatchers("/api/statuspage/{id}/public").permitAll()
                        //actuator requests
                        .requestMatchers("/app/**").permitAll()
                        //springdoc
                        .requestMatchers("/api-doc/**").permitAll()
                        .anyRequest().authenticated());

        //Add our custom JWT filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        //It can be placed anywhere
        http.addFilterAfter(actualOriginCookieFilter, JwtRequestFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
