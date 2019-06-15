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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public <T> Collector<T, ?, T> toMessage(ProcessedUserMessage currentMessage) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

}
