package org.codequistify.master.infrastructure.player.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.service.ProfanityChecker;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BadWordFilteringChecker implements ProfanityChecker {
    private final BadWordFiltering filtering;

    @Override
    public boolean hasProfanity(String input) {
        return filtering.check(input);
    }
}