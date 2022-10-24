package com.rakutenmobile.messageapi.lib.profanity;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Configuration
@PropertySource("classpath:application.properties")
public class DefaultConfig {

    @Resource
    public Environment env;

    @Bean
    @Qualifier("bad-words.source.env")
    public Pair<Map<String, String[]>, Integer> badWordsFromEnv() {
        Map<String, String[]> badWords = new HashMap<>();
        String rawBadWords = env.getRequiredProperty("app.bad-words");
        String[] words = rawBadWords.split(",");
        int largestWordLength = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > largestWordLength) {
                largestWordLength = word.length();
            }
            badWords.put(word.replaceAll(" ", "").toLowerCase(), new String[]{});
        }
        return new MutablePair<>(badWords, largestWordLength);
    }

    @Bean
    public DefaultImpl defaultProfanity(Pair<Map<String, String[]>, Integer> data) {
        return new DefaultImpl(data);
    }
}
