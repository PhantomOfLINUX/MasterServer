package org.codequistify.master.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {
    private final Logger LOGGER = LoggerFactory.getLogger(HttpLoggingInterceptor.class);

    private static String getOriginRemoteAddr(HttpServletRequest request) {
        String originAddr = request.getHeader("X-Real-IP");

        if (originAddr == null || originAddr.isBlank()) {
            originAddr = request.getRemoteAddr();
        }

        return originAddr;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
                                                                                                       Exception {
        String requestURI = request.getRequestURI();
        String requestAddr = getOriginRemoteAddr(request);
        String requestID = UUID.randomUUID().toString();

        request.setAttribute("RequestID", requestID);

        if (handler instanceof HandlerMethod handlerMethod) {
        }

        LOGGER.info("[API Request]  RequestID: {}, RequestURI: {}, RequestHost: {} Handler: {}",
                    requestID,
                    requestURI,
                    requestAddr,
                    handler);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String requestID = request.getAttribute("RequestID").toString();

        LOGGER.info("[API Response] RequestID: {}, RequestURI: {}, Handler: {}", requestID, requestURI, handler);

        if (ex != null) {
            LOGGER.error("[API Response] Error: ", ex);
        }
    }
}
