package org.codequistify.master.global.config;


import lombok.RequiredArgsConstructor;
import org.codequistify.master.global.filter.CustomCorsFilter;
import org.codequistify.master.global.filter.CustomCsrfFilter;
import org.codequistify.master.global.filter.CustomFormLoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.FormContentFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomCorsFilter customCorsFilter;
    private final CustomCsrfFilter customCsrfFilter;
    private final CustomFormLoginFilter customFormLoginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .addFilterBefore(customCorsFilter, CorsFilter.class)
                .addFilterBefore(customCsrfFilter, CsrfFilter.class)
                .addFilterAt(customFormLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}


