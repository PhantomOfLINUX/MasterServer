package org.codequistify.master.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.global.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    private final ArrayList<String> allowedPaths = new ArrayList<>(List.of(
            "/api/admin/",
            "/api/quest/"));
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
        if (!isAllowed) {
            filterChain.doFilter(request, response);
            LOGGER.info(response.getHeader("Access-Control-Allow-Origin"));
            return;
        }

        String token = tokenProvider.resolveToken(request);
        LOGGER.info("token : {}", token);
        if (token != null && tokenProvider.isValidatedToken(token)){
            //
        }
        else {
            response.setHeader("Authorization", token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("INVALID TOKEN");
            LOGGER.info("INVALID TOKEN");
            return;
        }

        response.setHeader("Authorization", token);
        filterChain.doFilter(request, response);
    }
}
