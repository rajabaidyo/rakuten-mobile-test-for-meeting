package com.rakutenmobile.messageapi.lib.profanity;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Map;

public class DefaultImpl implements Filter {

    private final Pair<Map<String, String[]>, Integer> data;
    private final Map<String, String[]> badWords;
    private final Integer largestWordLength;

    public DefaultImpl(@Qualifier("bad-words.source.env") Pair<Map<String, String[]>, Integer> data) {
        this.data = data;
        this.badWords = data.getKey();
        this.largestWordLength = data.getValue();
    }

    @Override
    public boolean check(String input) {
        if (input == null) {
            return false;
        }
        String modifiedInput = input;

        // remove leetspeak
        modifiedInput = modifiedInput.replaceAll("1", "i").replaceAll("!", "i").replaceAll("3", "e").replaceAll("4", "a")
                .replaceAll("@", "a").replaceAll("5", "s").replaceAll("7", "t").replaceAll("0", "o").replaceAll("9", "g");

        // ignore any character that is not a letter
        modifiedInput = modifiedInput.toLowerCase().replaceAll("[^a-zA-Z]", "");

        ArrayList<String> badWordsFound = new ArrayList<>();

        // iterate over each letter in the word
        for (int start = 0; start < modifiedInput.length(); start++) {
            // from each letter, keep going to find bad words until either the end of
            // the sentence is reached, or the max word length is reached.
            for (int offset = 1; offset < (modifiedInput.length() + 1 - start) && offset < largestWordLength; offset++) {
                String wordToCheck = modifiedInput.substring(start, start + offset);
                if (badWords.containsKey(wordToCheck)) {
                    String[] ignoreCheck = badWords.get(wordToCheck);
                    boolean ignore = false;
                    for (int stringIndex = 0; stringIndex < ignoreCheck.length; stringIndex++) {
                        if (modifiedInput.contains(ignoreCheck[stringIndex])) {
                            ignore = true;
                            break;
                        }
                    }

                    if (!ignore) {
                        badWordsFound.add(wordToCheck);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
