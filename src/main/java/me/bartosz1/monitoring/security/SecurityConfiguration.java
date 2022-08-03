package me.bartosz1.monitoring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private TokenRequestFilter tokenRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF will get enabled once I start doing frontend I think
        http.csrf().disable().authorizeRequests()
                //All index requests need to be allowed - POST for agent, GET for index site etc.
                .antMatchers("/").permitAll()
                //All login and register requests need to be allowed
                .antMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                //Allow all requests to Actuator endpoints - the only one exposed is /health
                .antMatchers("/app/**").permitAll()
                //Allow all requests to public statuspage endpoint(s)
                .antMatchers("/api/v1/statuspage/stats").permitAll()
                //Require authentication for all other requests
                .anyRequest().authenticated().and()
                //Use stateless sessions
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //Add our custom JWT filters
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tokenRequestFilter, JwtRequestFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
