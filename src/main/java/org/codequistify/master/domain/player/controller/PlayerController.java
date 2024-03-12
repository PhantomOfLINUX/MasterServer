package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.service.PlayerService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plyers")
@Tag(name = "Player")
public class PlayerController {
    private final PlayerService playerService;
    @LogMonitoring
    @GetMapping("")
    public ResponseEntity<List<PlayerProfile>> getAllPlayerProfiles() {
        List<PlayerProfile> response =  playerService.findAllPlayerProfiles();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
