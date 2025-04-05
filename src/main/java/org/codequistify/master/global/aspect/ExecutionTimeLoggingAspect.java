package org.codequistify.master.global.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ExecutionTimeLoggingAspect {
    private final Logger LOGGER = LoggerFactory.getLogger(MethodInvocationLoggingAspect.class);

    @Around("@annotation(LogExecutionTime) || @annotation(LogMonitoring)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object result = null;

        long startTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long durationTimeSec = endTime - startTime;
            LOGGER.info("[{}] 실행시간: {}ms", signature.getMethod().getName(), durationTimeSec);
        }

        return result;
    }
}
