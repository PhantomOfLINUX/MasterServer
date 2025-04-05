package org.codequistify.master.global.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Component
public class CustomCorsFilter extends CorsFilter {
    public CustomCorsFilter() {
        super(corsConfigurationSource());
    }

    private static CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
                                                      "https://localhost:3000",
                                                      "http://localhost:8080",
                                                      "https://www.pol.or.kr",
                                                      "https://test.www.pol.or.kr",
                                                      "https://api.pol.or.kr")); // 허용할 출처
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH")); // 허용할 HTTP 메소드
        configuration.setAllowedHeaders(Arrays.asList("Content-Type",
                                                      "Accept",
                                                      "Authorization",
                                                      "X-Real-IP")); // 허용할 헤더
        configuration.setAllowCredentials(true); // 쿠키 및 인증 정보 허용 설정
        configuration.setMaxAge(3600L); // 사전 요청 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대한 CORS 설정 적용

        return source;
    }
}
