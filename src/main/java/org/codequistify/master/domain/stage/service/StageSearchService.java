package org.codequistify.master.domain.stage.service;

import java.util.List;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.*;

public interface StageSearchService {
  // 스테이지 조회
  Stage getStageById(Long stageId);

  // 스테이지 코드 조회
  Stage getStageByCode(StageCode stageCode);

  // 스테이지 목록 조회
  StagePageResponse findStagesByCriteria(SearchCriteria searchCriteria, Player player);

  // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
  QuestionResponse findQuestion(Long stageId, Integer questionIndex);

  // 완료한 스테이지 목록 조회
  PlayerStageProgressResponse getCompletedStagesByPlayerId(PlayerId playerId);

  // 진행중인 스테이지 목록 조회
  PlayerStageProgressResponse getInProgressStagesByPlayerId(PlayerId playerId);
  // 완료한 날짜/횟수 기록 조회
  List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(PlayerId playerId);

  // preview 메서드
  StageResponse getStageByChoCho(String src);

  StageResponse getStageBySearchText(String searchText);
}
