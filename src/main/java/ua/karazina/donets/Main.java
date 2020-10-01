package ua.karazina.donets;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ua.karazina.donets.model.Data;
import ua.karazina.donets.service.TextLoader;
import ua.karazina.donets.service.TextProcessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ua.karazina.donets.service.TextProcessor.mapMessagesToWords;
import static ua.karazina.donets.service.TextStatistic.calculateAverageMessageLength;
import static ua.karazina.donets.service.TextStatistic.calculateAverageWordLength;
import static ua.karazina.donets.service.TextStatistic.calculateMessagesLengthCount;
import static ua.karazina.donets.service.TextStatistic.calculateWordsFrequency;
import static ua.karazina.donets.service.TextStatistic.calculateWordsFrequencyMap;
import static ua.karazina.donets.service.TextStatistic.calculateWordsLengthCount;

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
        lab1();
        lab2();
    }

    private void saveSortedByFrequency(List<Map.Entry<String, Integer>> wordCount, String fileName) {
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            wordCount.forEach(entry ->
                    printWriter.println(entry.getKey() + " : " + entry.getValue()));
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with saving file with fileName: " + fileName, e);
        }
    }

    private void lab1(){
        Data data = TextLoader.loadData(new File("sms-spam-corpus.csv"));

        //processing of messages
        List<String> hamMessages = TextProcessor.processText(data.getHamMessages());
        List<String> spamMessages = TextProcessor.processText(data.getSpamMessages());

        // calculating frequency of words
        List<Map.Entry<String, Integer>> hamsFrequency = calculateWordsFrequency(hamMessages);
        List<Map.Entry<String, Integer>> spamsFrequency = calculateWordsFrequency(spamMessages);

        // saving frequency of words
        saveSortedByFrequency(hamsFrequency, "hams.txt");
        saveSortedByFrequency(spamsFrequency, "spams.txt");

        // task 1
        addAllToChart(calculateWordsLengthCount(hamMessages), "#chart11");
        addAllToChart(calculateWordsLengthCount(spamMessages), "#chart12");
        Label label11 = (Label) scene.lookup("#label11");
        Label label12 = (Label) scene.lookup("#label12");
        label11.setText("average word length for hams : " + calculateAverageWordLength(hamMessages));
        label12.setText("average word length for spams: " + calculateAverageWordLength(spamMessages));

        // task 2
        addAllToChart(calculateMessagesLengthCount(hamMessages), "#chart21");
        addAllToChart(calculateMessagesLengthCount(spamMessages), "#chart22");
        Label label21 = (Label) scene.lookup("#label21");
        Label label22 = (Label) scene.lookup("#label22");
        label21.setText("average message length for hams : " + calculateAverageMessageLength(hamMessages));
        label22.setText("average message length for spams: " + calculateAverageMessageLength(spamMessages));

        // task 3
        addTwentyMostFrequentWordsToChart(hamsFrequency, "#chart31");
        addTwentyMostFrequentWordsToChart(spamsFrequency, "#chart32");
    }

    private void lab2() {
        TextField lab2FilePath = (TextField) scene.lookup("#lab2FilePath");
        TextArea lab2TextArea = (TextArea) scene.lookup("#lab2TextArea");
        Button calculateButton = (Button) scene.lookup("#lab2Calculate");

        TextField lab2SpamProbability = (TextField) scene.lookup("#lab2SpamProbability");
        TextField lab2HamProbability = (TextField) scene.lookup("#lab2HamProbability");

        calculateButton.setOnMouseClicked(event -> {
            Data l2data = TextLoader.loadData(new File(lab2FilePath.getText()));
            List<String> l2hamMessages = TextProcessor.processText(l2data.getHamMessages());
            List<String> l2spamMessages = TextProcessor.processText(l2data.getSpamMessages());

            int countHamWords = mapMessagesToWords(l2hamMessages).size();
            int countSpamWords = mapMessagesToWords(l2spamMessages).size();

            double Pham = (double) countHamWords / (countHamWords + countSpamWords);
            double Pspam = (double) countSpamWords / (countHamWords + countSpamWords);

            List<String> wordsOfMessage = mapMessagesToWords(TextProcessor.processMessage(lab2TextArea.getText()));


            Map<String, Integer> l2hamsFrequency = calculateWordsFrequencyMap(l2hamMessages);
            Map<String, Integer> l2spamsFrequency = calculateWordsFrequencyMap(l2spamMessages);


            long countOfAbsentWordsHam = wordsOfMessage.stream()
                    .filter(key -> l2hamsFrequency.containsKey(key))
                    .count();
            long countOfAbsentWordsSpam = wordsOfMessage.stream()
                    .filter(key -> l2spamsFrequency.containsKey(key))
                    .count();

            double PbodyHam = wordsOfMessage.stream()
                    .map(word -> l2hamsFrequency.get(word))
                    .filter(Objects::nonNull)
                    .map(value -> value + 1)
                    .mapToDouble(value -> (double) value / (countHamWords + countOfAbsentWordsHam))
                    .reduce(1, (a, b) -> a * b);

            double PbodySpam = wordsOfMessage.stream()
                    .map(word -> l2spamsFrequency.get(word))
                    .filter(Objects::nonNull)
                    .map(value -> value + 1)
                    .mapToDouble(value -> (double) value / (countSpamWords + countOfAbsentWordsSpam))
                    .reduce(1, (a, b) -> a * b);

            double hamResult = Pham * PbodyHam;
            double spamResult = Pspam * PbodySpam;

            lab2HamProbability.setText(String.valueOf(hamResult));
            lab2SpamProbability.setText(String.valueOf(spamResult));
        });
    }

    private void addAllToChart(List<Map.Entry<Integer, Integer>> wordLengthCount, String chartSelector) {
        float max = wordLengthCount.stream().map(entry -> entry.getValue()).mapToInt(Integer::intValue).sum();

        BarChart barChart = (BarChart) scene.lookup(chartSelector);
        XYChart.Series<String, Float> series = new XYChart.Series<>();
        wordLengthCount.stream().forEach(entry ->
                series.getData().add(
                        new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue().floatValue() / max)
                )
        );
        barChart.getData().addAll(series);
    }

    private void addTwentyMostFrequentWordsToChart(List<Map.Entry<String, Integer>> wordsCount, String chartSelector) {
        float max = wordsCount.stream().map(entry -> entry.getValue()).mapToInt(Integer::intValue).sum();

        BarChart barChart = (BarChart) scene.lookup(chartSelector);
        XYChart.Series<String, Float> series = new XYChart.Series<>();
        wordsCount.stream().limit(20).forEach(entry ->
                series.getData().add(
                        new XYChart.Data<>(entry.getKey(), entry.getValue().floatValue() / max)
                )
        );
        barChart.getData().addAll(series);
    }
}
