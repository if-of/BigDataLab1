package ua.karazina.donets.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextStatistic {

    public static List<Map.Entry<String, Integer>> calculateWordsFrequency(List<String> stringList) {
        Map<String, Integer> frequency = new HashMap<>();

        stringList.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .forEach(word ->
                        frequency.compute(word, (k, v) -> (v == null) ? 1 : v + 1)
                );

        List<Map.Entry<String, Integer>> list = new ArrayList<>(frequency.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return list;
    }

    public static Map<String, Integer> calculateWordsFrequencyMap(List<String> stringList) {
        Map<String, Integer> frequency = new HashMap<>();

        stringList.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .forEach(word ->
                        frequency.compute(word, (k, v) -> (v == null) ? 1 : v + 1)
                );

        return frequency;
    }


    public static List<Map.Entry<Integer, Integer>> calculateWordsLengthCount(List<String> stringList) {
        Map<Integer, Integer> lengthCount = new HashMap<>();

        stringList.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .map(String::length)
                .forEach(length ->
                        lengthCount.compute(length, (k, v) -> (v == null) ? 1 : v + 1));

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(lengthCount.entrySet());
        list.sort(Map.Entry.comparingByKey());
        return list;
    }

    public static List<Map.Entry<Integer, Integer>> calculateMessagesLengthCount(List<String> stringList) {
        Map<Integer, Integer> lengthCount = new HashMap<>();

        stringList.stream()
                .map(String::length)
                .forEach(length ->
                        lengthCount.compute(length, (k, v) -> (v == null) ? 1 : v + 1));

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(lengthCount.entrySet());
        list.sort(Map.Entry.comparingByKey());
        return list;
    }


    public static double calculateAverageWordLength(List<String> stringList) {
        List<String> wordList = stringList.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        return calculateAverageMessageLength(wordList);
    }

    public static double calculateAverageMessageLength(List<String> stringList) {
        double totalLength = 0;
        double count = 0;

        List<Integer> lengths = stringList.stream()
                .map(String::length)
                .collect(Collectors.toList());

        for (Integer length : lengths) {
            count++;
            totalLength += length;
        }

        return totalLength / count;
    }
}
