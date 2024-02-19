package org.codequistify.master.domain.stage.service;

import org.codequistify.master.domain.stage.dto.*;

public interface StageService {
    // 스테이지 저장
    void saveStage(StageRegistryRequest request);
    // 스테이지 목록 조회
    StagePageResponse findStageByGroup(SearchCriteria searchCriteria);
    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    QuestionResponse findQuestion(Long stageId, Integer questionIndex);
    // 문항 채점 요청
    GradingResponse checkAnswerCorrectness(GradingRequest request);
    // 스테이지 수정
    // 스테이지 문항 수정
    // 스테이지 옵션 수정
}
