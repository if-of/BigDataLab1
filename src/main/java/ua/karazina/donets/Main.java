package ua.karazina.donets;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ua.karazina.donets.service.Stemmer;
import ua.karazina.donets.utils.ResourceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main extends Application {

    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass()
                .getClassLoader()
                .getResource("sample.fxml"));

        primaryStage.setTitle("lab1");
        primaryStage.setScene(new Scene(root, 1055, 655));
        primaryStage.show();

        this.scene = primaryStage.getScene();
        mainLogic();
    }

    private void mainLogic() {
        String wholeFile = ResourceUtils.readFileFromResourceAsString("sms-spam-corpus.csv");
        String[] lines = wholeFile.split("\\R*,,,\\R*");

        /**
         * splitting messages into 2 categories
         */
        List<String> hams = new ArrayList<>();
        List<String> spams = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].startsWith("ham,")) {
                hams.add(lines[i].substring(4));
            } else if (lines[i].startsWith("spam,")) {
                spams.add(lines[i].substring(5));
            } else {
                throw new RuntimeException("Unexpecting category: " + lines[i]);
            }
        }


        /**
         * processing of messages
         */
        List<String> processedHams = processMessages(hams);
        List<String> processedSpams = processMessages(spams);


        /**
         * calculating frequency of words
         */
        List<Map.Entry<String, Integer>> hamsFrequency = calculateFrequency(processedHams);
        List<Map.Entry<String, Integer>> spamsFrequency = calculateFrequency(processedSpams);

        /**
         * saving frequency of words
         */
        saveSortedByFrequency(hamsFrequency, "hams.txt");
        saveSortedByFrequency(spamsFrequency, "spams.txt");


        /**
         * task 1
         */
        addAllToChart(calculateWordLengthCount(processedHams), "#chart11");
        addAllToChart(calculateWordLengthCount(processedSpams), "#chart12");
        Label label11 = (Label) scene.lookup("#label11");
        Label label12 = (Label) scene.lookup("#label12");
        label11.setText("average word length for hams : " + calculateAverageWordLength(processedHams));
        label12.setText("average word length for spams: " + calculateAverageWordLength(processedSpams));


        /**
         * task 2
         */
        addAllToChart(calculateMessageLengthCount(processedHams), "#chart21");
        addAllToChart(calculateMessageLengthCount(processedSpams), "#chart22");
        Label label21 = (Label) scene.lookup("#label21");
        Label label22 = (Label) scene.lookup("#label22");
        label21.setText("average message length for hams : " + calculateAverageMessageLength(processedHams));
        label22.setText("average message length for spams: " + calculateAverageMessageLength(processedSpams));


        /**
         * task 3
         */
        addTwentyMostFrequentWordsToChart(hamsFrequency, "#chart31");
        addTwentyMostFrequentWordsToChart(spamsFrequency, "#chart32");
    }

    private static List<String> processMessages(List<String> strings) {
        return strings.stream()
                .map(s -> s.replaceAll("[^a-zA-Z\\s]", ""))
                .map(String::toLowerCase)
                .map(Main::removeStopWords)
                .map(Main::stem)
                .map(String::trim)
                .map(s -> s.replaceAll("\\s++", " "))
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


    private static List<Map.Entry<String, Integer>> calculateFrequency(List<String> stringList) {
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

    private static void saveSortedByFrequency(List<Map.Entry<String, Integer>> wordCount, String fileName) {
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            wordCount.forEach(entry ->
                    printWriter.println(entry.getKey() + " : " + entry.getValue()));
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with saving file with fileName: " + fileName, e);
        }
    }


    private static List<Map.Entry<Integer, Integer>> calculateWordLengthCount(List<String> stringList) {
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

    private static double calculateAverageWordLength(List<String> stringList) {
        double totalLength = 0;
        double count = 0;

        List<Integer> lengths = stringList.stream()
                .map(s -> s.split("\\h"))
                .flatMap(Arrays::stream)
                .map(String::length)
                .collect(Collectors.toList());

        for (Integer length : lengths) {
            count++;
            totalLength += length;
        }

        return totalLength / count;
    }


    private static List<Map.Entry<Integer, Integer>> calculateMessageLengthCount(List<String> stringList) {
        Map<Integer, Integer> lengthCount = new HashMap<>();

        stringList.stream()
                .map(String::length)
                .forEach(length ->
                        lengthCount.compute(length, (k, v) -> (v == null) ? 1 : v + 1));

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(lengthCount.entrySet());
        list.sort(Map.Entry.comparingByKey());
        return list;
    }

    private static double calculateAverageMessageLength(List<String> stringList) {
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


    private void addAllToChart(List<Map.Entry<Integer, Integer>> wordLengthCount, String chartSelector) {
        BarChart barChart = (BarChart) scene.lookup(chartSelector);
        XYChart.Series<String, Float> series = new XYChart.Series<>();
        wordLengthCount.stream().forEach(entry ->
                series.getData().add(
                        new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue().floatValue())
                )
        );
        barChart.getData().addAll(series);
    }

    private void addTwentyMostFrequentWordsToChart(List<Map.Entry<String, Integer>> wordsCount, String chartSelector) {
        BarChart barChart = (BarChart) scene.lookup(chartSelector);
        XYChart.Series<String, Float> series = new XYChart.Series<>();
        wordsCount.stream().limit(20).forEach(entry ->
                series.getData().add(
                        new XYChart.Data<>(entry.getKey(), entry.getValue().floatValue())
                )
        );
        barChart.getData().addAll(series);
    }
}
