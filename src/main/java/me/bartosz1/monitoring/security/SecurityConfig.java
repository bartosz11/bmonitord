package me.bartosz1.monitoring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF will get enabled once I start doing frontend I think
        http.cors().and().csrf().disable().authorizeHttpRequests()
                //Frontend requests
                .requestMatchers("/").permitAll()
                .requestMatchers("/index.html").permitAll()
                .requestMatchers("/assets/**").permitAll()
                //All login and register requests need to be allowed
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                //Allow all requests agents make - install signal, post data
                .requestMatchers("/api/agent/**").permitAll()
                //Public data
                .requestMatchers("/api/heartbeat/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/monitor/{id}").permitAll()
                .requestMatchers("/api/monitor/{monitorId}/agent").permitAll()
                .requestMatchers("/api/incident/**").permitAll()
                .requestMatchers("/api/statuspage/{id}/public").permitAll()
                //Allow all requests to Actuator endpoints - the only one exposed is /health
                .requestMatchers("/app/**").permitAll()
                //springdoc
                .requestMatchers("/api-doc/**").permitAll()
                //Require authentication for all other requests
                .anyRequest().authenticated().and()
                //Use stateless sessions
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //Add our custom JWT filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
