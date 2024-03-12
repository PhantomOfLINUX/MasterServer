package org.codequistify.master.domain.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerConverter playerConverter;

    @LogMonitoring
    public List<PlayerProfile> findAllPlayerProfiles() {
        return playerRepository.findAll().stream()
                .map(playerConverter::convert)
                .collect(Collectors.toList());
    }
}
