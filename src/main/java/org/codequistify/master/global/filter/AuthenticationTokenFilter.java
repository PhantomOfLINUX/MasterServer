package org.codequistify.master.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.service.impl.PlayerDetailsService;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final PlayerDetailsService playerDetailsService;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    @Value("${host.develop.api.ant-match.uri}")
    private List<String> antMatchURIs = new ArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        LOGGER.info("host: {}, path: {}", request.getRemoteHost(), path);

        // 토큰 비검사 uri
        for (String antMatchURI : antMatchURIs) {
            if (path.startsWith(antMatchURI)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = tokenProvider.resolveToken(request);
        if (token == null) {
            LOGGER.info("[TokenFilter] 토큰이 비어 있는 요청");
            doFalseAction(response);
            return;
        }

        Claims claims = tokenProvider.getClaims(token);
        if (claims == null || !tokenProvider.checkExpire(claims)) {
            LOGGER.info("[TokenFilter] 올바르지 않은 토큰 상태");
            doFalseAction(response);
            return;
        }

        String aud = claims.getAudience();
        if (aud == null || aud.isBlank()) {
            LOGGER.info("[TokenFilter] 올바르지 않은 aud 값");
            doFalseAction(response);
            return;
        }

        UserDetails userDetails = playerDetailsService.loadUserByUsername(aud);
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        response.setHeader("Authorization", token);
        filterChain.doFilter(request, response);
    }

    private void doFalseAction(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(new BasicResponse(null, "올바르지 않은 토큰 정보입니다."))
        );
    }
}
