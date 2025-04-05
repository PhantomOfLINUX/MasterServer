package org.codequistify.master.application.account.config;

import org.codequistify.master.core.domain.player.service.PlayerValidator;
import org.codequistify.master.core.domain.player.service.ProfanityChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerValidatorConfig {

    @Bean
    public PlayerValidator playerValidator(ProfanityChecker profanityChecker) {
        return new PlayerValidator(profanityChecker);
    }
}
