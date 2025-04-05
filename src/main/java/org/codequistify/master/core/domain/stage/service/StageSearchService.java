package org.codequistify.master.core.domain.stage.service;

import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.application.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.codequistify.master.core.domain.stage.dto.*;

import java.util.List;

public interface StageSearchService {
    // 스테이지 조회
    Stage getStageById(Long stageId);

    // 스테이지 목록 조회
    StagePageResponse findStagesByCriteria(SearchCriteria searchCriteria, Player player);

    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    QuestionResponse findQuestion(Long stageId, Integer questionIndex);

    // 완료한 스테이지 목록 조회
    PlayerStageProgressResponse getCompletedStagesByPlayerId(PolId playerId);

    // 진행중인 스테이지 목록 조회
    PlayerStageProgressResponse getInProgressStagesByPlayerId(PolId playerId);
    // 완료한 날짜/횟수 기록 조회
    List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(PolId playerId);

    //preview 메서드
    StageResponse getStageByChoCho(String src);
    StageResponse getStageBySearchText(String searchText);
}
