package org.codequistify.master.domain.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.domain.stage.dto.HeatMapDataPoint;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerProfileService {
    private final PlayerRepository playerRepository;
    private final PlayerConverter playerConverter;
    private final StageSearchService stageSearchService;

    @LogExecutionTime
    @Transactional
    public List<PlayerProfile> findAllPlayerProfiles() {
        return playerRepository.findAll().stream()
                .map(playerConverter::convert)
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    @Transactional
    public PlayerStageProgressResponse getCompletedStagesByPlayerId(Player player) {
        return stageSearchService.getCompletedStagesByPlayerId(player.getId());
    }

    @LogExecutionTime
    @Transactional
    public PlayerStageProgressResponse getInProgressStagesByPlayerId(Player player) {
        return stageSearchService.getInProgressStagesByPlayerId(player.getId());
    }

    @LogExecutionTime
    @Transactional
    public List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(Player player) {
        return stageSearchService.getHeatMapDataPointsByModifiedDate(player.getId());
    }
}
