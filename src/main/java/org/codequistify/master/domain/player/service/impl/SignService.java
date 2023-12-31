package org.codequistify.master.domain.player.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.repository.PlayerRepository;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignRequest;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class SignService {
    private final PlayerRepository playerRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(SignService.class);

    public PlayerDTO signUp(SignRequest request) {
        if (playerRepository.findByEmail(request.email()).isPresent()){
            LOGGER.info("[signUp] 이미 존재하는 email 입니다.");
            throw new EntityExistsException("이미 존재하는 email입니다.");
        }

        Player player = request.toPlayer();

        player.encodePassword();
        playerRepository.save(player);

        LOGGER.info("[signIn] {} player 회원가입 완료", player.getId());

        return player.toPlayerDTO();
    }

    public PlayerDTO signIn(SignRequest request) {
        Player player = playerRepository.findByEmail(request.email())
                .orElseThrow(() ->{
                    LOGGER.info("[signIn] 존재하지 않는 email 입니다.");
                    return new EntityNotFoundException("존재하지 않는 email 입니다");
                });

        if (player.decodePassword(request.password())){
            return player.toPlayerDTO();
        }else {
            throw new IllegalArgumentException("email 또는 password가 잘못되었습니다");
        }
    }

    public boolean checkEmailDuplication(String email){
        return playerRepository.findByEmail(email).isPresent();
    }
}
