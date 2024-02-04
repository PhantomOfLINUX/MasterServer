package org.codequistify.master.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    private final ArrayList<String> allowedPaths = new ArrayList<>(List.of(
            "/api/admin/",
            "/api/stage/"));
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        LOGGER.info("path: {}", path);
        boolean isAllowed = false;

        for (String allowedPath : allowedPaths) {
            if (path.startsWith(allowedPath)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) { //동록되지 않은 path는 통과
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenProvider.resolveToken(request);
        LOGGER.info("token : {}", token);
        Claims claims = tokenProvider.getClaims(token);
        if (claims != null && tokenProvider.checkExpire(claims)){
            String aud = claims.getAudience();

            if (aud != null && !aud.isBlank()) {
                List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
                UsernamePasswordAuthenticationToken auth
                        = new UsernamePasswordAuthenticationToken(aud, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(new BasicResponse(null, ""))
            );

            LOGGER.info("[TokenFilter] 올바르지 않은 토큰 정보입니다.");
            return;
        }

        response.setHeader("Authorization", token);
        filterChain.doFilter(request, response);
    }
}
