package org.codequistify.master.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.util.BasicResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BusinessExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        }
        catch (BusinessException exception) {
            writeErrorCode(request, response, exception);
        }
    }

    private void writeErrorCode(HttpServletRequest request, HttpServletResponse response, BusinessException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        BasicResponse.of(exception)
                )
        );
    }
}
