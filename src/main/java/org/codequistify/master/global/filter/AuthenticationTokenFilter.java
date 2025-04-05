package org.codequistify.master.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.infrastructure.security.TokenPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationTokenFilter.class);
    private final TokenProvider tokenProvider;
    @Value("${host.develop.api.ant-match.uri}")
    private List<String> antMatchURIs = new ArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 인증 제외 경로 처리
        if (antMatchURIs.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = tokenProvider.resolveToken(request);
            TokenPlayer tokenPlayer = tokenProvider.extractTokenPlayer(token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    tokenPlayer,
                    null,
                    tokenPlayer.getRoles().stream()
                               .map(SimpleGrantedAuthority::new)
                               .collect(Collectors.toSet())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("[AuthenticationTokenFilter] 인증 완료 - uid: {}, email: {}",
                        tokenPlayer.getUid(),
                        tokenPlayer.getEmail());

        } catch (ApplicationException e) {
            logger.warn("[AuthenticationTokenFilter] 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            throw e; // 전역 에러 핸들러로 넘김
        }

        filterChain.doFilter(request, response);
    }
}
