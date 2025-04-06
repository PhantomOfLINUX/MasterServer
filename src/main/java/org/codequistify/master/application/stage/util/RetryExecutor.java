package org.codequistify.master.application.stage.util;

import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.function.IntPredicate;

public class RetryExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryExecutor.class);

    private RetryExecutor() {
        throw new UnsupportedOperationException("RetryExecutor is a utility class.");
    }

    /**
     * 지정된 조건이 true가 될 때까지 재시도합니다.
     */
    public static void retryUntil(IntPredicate condition, int threshold, long intervalMillis, String context) {
        int retryCount = 0;

        while (true) {
            boolean success = false;

            try {
                success = condition.test(retryCount);
            } catch (Exception e) {
                LOGGER.warn("[{}] 조건 검사 중 예외 발생: {}", context, e.getMessage());
            }

            if (success) {
                return;
            }

            if (retryCount >= threshold) {
                LOGGER.error("[{}] 재시도 한도 초과", context);
                throw new ApplicationException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("[{}] 인터럽트 발생", context);
                throw new ApplicationException(ErrorCode.PSHELL_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            retryCount++;
        }
    }
}
