package org.codequistify.master.domain.stage.service;

import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.dto.GradingRequest;
import org.codequistify.master.domain.stage.dto.GradingResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;

public interface StageManagementService {
    // 스테이지 저장
    void saveStage(StageRegistryRequest request);

    // 문항 채점 요청
    GradingResponse checkAnswerCorrectness(GradingRequest request);

    // 풀이 완료 기록
    void recordStageComplete(Player player, Long stageId);

    // 풀이 시작 기록
    void recordInProgressStageInit(Player player, GradingRequest request);

    // 문제 풀이 정보 기록
    public void updateInProgressStage(Player player, GradingRequest request);


    // 스테이지 수정
    // 스테이지 문항 수정
    // 스테이지 옵션 수정



}
