package com.slobo.master.processor;

import com.slobo.master.model.ProcessedUserMessage;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class UserMessagePreProcessor {

    @Value("${processor.sentiment.distance}")
    private int sentimentDistance;
    @Autowired
    private StanfordCoreNLP pipeline;

    public ProcessedUserMessage process(String data) {
        if (StringUtils.isBlank(data)) {
            return null;
        }

        Annotation document = new Annotation(data);
        pipeline.annotate(document);

        Map<String, Integer> posStatistics = new HashMap<>();
        StringBuilder lemmatizatedMessage = new StringBuilder();
        int sentimentScore = 0;

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {

            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            sentimentScore = RNNCoreAnnotations.getPredictedClass(tree) + sentimentScore;

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                lemmatizatedMessage.append(lemma).append(" ");

                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                posStatistics.merge(pos, 1, Integer::sum);

            }
        }

        return new ProcessedUserMessage(lemmatizatedMessage.toString(), posStatistics, sentimentScore);
    }

    public Predicate<? super ProcessedUserMessage> bySentimentDistance(ProcessedUserMessage currentMessage) {
        return message -> Math.hypot(currentMessage.getSentimentScore(), message.getSentimentScore()) < sentimentDistance;
    }

    public Predicate<? super ProcessedUserMessage> byPOSDistance(ProcessedUserMessage currentMessage) {
        return message -> {
            Map<String, Integer> savedMessagePosStatistics = message.getPosStatistics();
            Map<String, Integer> currentMessagePosStatistics = currentMessage.getPosStatistics();

            for (Map.Entry<String, Integer> currentEntry : currentMessagePosStatistics.entrySet()) {
                savedMessagePosStatistics.merge(currentEntry.getKey(), currentEntry.getValue(), Integer::sum);
            }

            Integer distance = savedMessagePosStatistics.entrySet().stream()
                    .reduce((firstMessage, secondMessage) -> {
                        if (firstMessage.getValue() == 0) {
                            return secondMessage;
                        } else if (secondMessage.getValue() == 0) {
                            return firstMessage;
                        } else {
                            int difference = firstMessage.getValue() + secondMessage.getValue();
                            return new AbstractMap.SimpleEntry<>(firstMessage.getKey() + secondMessage.getKey() + difference, difference);
                        }
                    })
                    .orElseThrow(IllegalStateException::new).getValue();

            double border = Math.sqrt(currentMessage.getMessage().length());
            return distance < border;
        };
    }

    public Collector<ProcessedUserMessage, ?, ProcessedUserMessage> toMessage(ProcessedUserMessage currentMessage) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    String message = currentMessage.getMessage();
                    list.sort(Comparator.comparingInt(f -> calculateLevenshteinDistance(f.getMessage(), message)));
                    return list.get(0);
                }
        );
    }

    public int calculateLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

}
