package org.codequistify.master.global.config;


import lombok.RequiredArgsConstructor;
import org.codequistify.master.global.filter.AuthenticationTokenFilter;
import org.codequistify.master.global.filter.CustomCorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomCorsFilter customCorsFilter;
    private final AuthenticationTokenFilter authenticationTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .addFilterBefore(customCorsFilter, CorsFilter.class)
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // 폼 로그인 비활성화
                .formLogin(form -> form.disable())
                // jwt 인증 토큰 설정
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}


