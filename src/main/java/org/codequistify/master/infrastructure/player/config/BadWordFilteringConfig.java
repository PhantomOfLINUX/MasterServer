package org.codequistify.master.infrastructure.player.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BadWordFilteringConfig {
    private final ResourceLoader resourceLoader;

    @Bean
    public BadWordFiltering badWordFiltering() throws IOException {
        BadWordFiltering badWordFiltering = new BadWordFiltering();
        List<String> filteringWords = loadWordsAsList();
        badWordFiltering.addAll(filteringWords);

        return badWordFiltering;
    }

    private List<String> loadWordsAsList() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:words/words.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
        JsonNode wordsNode = rootNode.get("words");

        List<String> words = new ArrayList<>();
        wordsNode.forEach(word -> words.add(word.asText()));
        return words;
    }
}
