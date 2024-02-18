package org.codequistify.master.global.util;

import org.codequistify.master.domain.stage.dto.SearchCriteria;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class Validator {
    public boolean isValid(SearchCriteria searchCriteria) {
        if (searchCriteria.page_index() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_CRITERIA, HttpStatus.BAD_REQUEST);
        }
        if (searchCriteria.page_size() <= 0 || searchCriteria.page_size() % 10 != 0) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_CRITERIA, HttpStatus.BAD_REQUEST);
        }
        return true;
    }
}
