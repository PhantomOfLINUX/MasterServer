package org.codequistify.master.application.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PlayerRoleType;
import org.codequistify.master.core.domain.player.service.PlayerRolesChecker;
import org.codequistify.master.core.domain.stage.dto.HeatMapDataPoint;
import org.codequistify.master.core.domain.stage.service.StageSearchService;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.player.repository.PlayerJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerProfileService {
    private final PlayerJpaRepository playerJpaRepository;
    private final StageSearchService stageSearchService;

    @LogExecutionTime
    @Transactional(readOnly = true)
    public List<Player> findAllPlayerProfiles() {
        return playerJpaRepository.findAll().stream()
                                  .map(PlayerConverter::toDomain)
                                  .toList();
    }

    @LogExecutionTime
    @Transactional
    public PlayerStageProgressResponse getCompletedStagesByPlayerId(Player player) {
        return stageSearchService.getCompletedStagesByPlayerId(player.getUid());
    }

    @LogExecutionTime
    @Transactional
    public PlayerStageProgressResponse getInProgressStagesByPlayerId(Player player) {
        return stageSearchService.getInProgressStagesByPlayerId(player.getUid());
    }

    @LogExecutionTime
    @Transactional
    public List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(Player player) {
        return stageSearchService.getHeatMapDataPointsByModifiedDate(player.getUid());
    }

    public boolean isAdmin(Player player) {
        return (PlayerRolesChecker
                .checkAnyRole(
                        player,
                        List.of(PlayerRoleType.ADMIN.getRole(), PlayerRoleType.SUPER_ADMIN.getRole())));
    }

    @Transactional
    public Integer increaseExp(Player player, int point) {
        int exp = player.increaseLevelPoint(point).getExp();
        playerJpaRepository.save(PlayerConverter.toEntity(player));
        return exp;
    }

    @Transactional
    public boolean isDuplicatedName(String name) {
        return playerJpaRepository.existsByNameIgnoreCase(name);
    }


}
