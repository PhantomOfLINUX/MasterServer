package org.codequistify.master.domain.stage.service;

import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.QuestionResponse;
import org.codequistify.master.domain.stage.dto.SearchCriteria;
import org.codequistify.master.domain.stage.dto.StagePageResponse;

public interface StageSearchService {
    // 스테이지 조회
    Stage getStageById(Long stageId);

    // 스테이지 목록 조회
    StagePageResponse findStagesByCriteria(SearchCriteria searchCriteria, Player player);

    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    QuestionResponse findQuestion(Long stageId, Integer questionIndex);

    // 완료한 스테이지 목록 조회
    PlayerStageProgressResponse getCompletedStagesByPlayerId(Long playerId);

    // 진행중인 스테이지 목록 조회
    PlayerStageProgressResponse getInProgressStagesByPlayerId(Long playerId);
}
