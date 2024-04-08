package org.codequistify.master.domain.stage.service;

import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.CompletedStatus;
import org.codequistify.master.domain.stage.dto.GradingRequest;
import org.codequistify.master.domain.stage.dto.GradingResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;

public interface StageManagementService {
    // 스테이지 저장
    void saveStage(StageRegistryRequest request);

    // 문항 채점 요청
    GradingResponse checkAnswerCorrectness(GradingRequest request);

    // 풀이 완료 기록
    void recordStageComplete(Long stageId, Player player, CompletedStatus status);

    // 스테이지 수정
    // 스테이지 문항 수정
    // 스테이지 옵션 수정



}
