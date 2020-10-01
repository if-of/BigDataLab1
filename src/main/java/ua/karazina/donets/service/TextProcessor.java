package ua.karazina.donets.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TextProcessor {

    public static List<String> processMessages(List<String> strings) {
        return strings.stream()
                .map(s -> s.replaceAll("[^a-zA-Z\\s]", ""))
                .map(String::toLowerCase)
                .map(TextProcessor::removeStopWords)
                .map(TextProcessor::stem)
                .map(String::trim)
                .map(s -> s.replaceAll("\\s++", " "))
                .collect(Collectors.toList());
    }

    public static List<String> processMessage(String message) {
        return processMessages(Collections.singletonList(message));
    }

    public static List<String> mapMessagesToWords(List<String> strings) {
        return strings.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    private static String removeStopWords(String string) {
        String[] STOP_WORDS = {"a", "the", "to", "in"};
        String SPACES = "\\h++";

        for (String stopWord : STOP_WORDS) {
            string = string.replaceAll(SPACES + stopWord + SPACES, " ");
        }
        return string;
    }

    private static String stem(String string) {
        return Arrays.stream(string.split("\\h++"))
                .map(word -> new Stemmer().stem(word))
                .collect(Collectors.joining(" "));
    }
}
