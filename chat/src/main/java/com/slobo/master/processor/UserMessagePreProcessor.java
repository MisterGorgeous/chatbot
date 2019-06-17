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
import edu.stanford.nlp.util.PropertiesUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UserMessagePreProcessor
{

    @Value("${processor.sentiment.distance}")
    private int sentimentDistance;
    @Autowired
    private StanfordCoreNLP pipeline;

    public ProcessedUserMessage process(String data)
    {
        if (StringUtils.isBlank(data))
        {
            return null;
        }

        Annotation document = new Annotation(data);
        pipeline.annotate(document);

        Map<String, Integer> posStatistics = new HashMap<>();
        StringBuilder lemmatizatedMessage = new StringBuilder();
        int sentimentScore = 0;

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences)
        {

            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            sentimentScore = RNNCoreAnnotations.getPredictedClass(tree) + sentimentScore;

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class))
            {

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                lemmatizatedMessage.append(lemma).append(" ");

                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.equals("."))
                {
                    posStatistics.merge(pos, 1, Integer::sum);
                }

            }
        }

        return new ProcessedUserMessage(lemmatizatedMessage.toString(), posStatistics, sentimentScore);
    }

    public Predicate<? super ProcessedUserMessage> bySentimentDistance(ProcessedUserMessage currentMessage)
    {
        return message -> Math.abs(currentMessage.getSentimentScore()) - Math.abs(message.getSentimentScore()) < 3;
    }

    public Predicate<? super ProcessedUserMessage> byPOSDistance(ProcessedUserMessage currentMessage)
    {
        return message -> {
            Map<String, Integer> savedMessagePosStatistics = message.getPosStatistics();
            Map<String, Integer> currentMessagePosStatistics = currentMessage.getPosStatistics();

            double border = Stream
                    .of(savedMessagePosStatistics.values().stream(), currentMessagePosStatistics.values().stream())
                    .flatMap(i -> i)
                    .reduce(Integer::sum)
                    .orElse(0);

            for (Map.Entry<String, Integer> currentEntry : currentMessagePosStatistics.entrySet())
            {
                savedMessagePosStatistics
                        .merge(currentEntry.getKey(), currentEntry.getValue(), (f, s) -> Math.abs(f - s));
            }

            Integer distance = savedMessagePosStatistics.entrySet().stream()
                    .reduce((firstMessage, secondMessage) -> {
                        if (firstMessage.getValue() == 0)
                        {
                            return secondMessage;
                        }
                        else if (secondMessage.getValue() == 0)
                        {
                            return firstMessage;
                        }
                        else
                        {
                            int difference = firstMessage.getValue() + secondMessage.getValue();
                            return new AbstractMap.SimpleEntry<>(
                                    firstMessage.getKey() + secondMessage.getKey() + difference, difference);
                        }
                    })
                    .orElseThrow(IllegalStateException::new).getValue();

            return distance < border / 2;
        };
    }

    public Collector<ProcessedUserMessage, ?, ProcessedUserMessage> toMessage(ProcessedUserMessage currentMessage)
    {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    String message = currentMessage.getMessage();
                    ImmutablePair<ProcessedUserMessage, Integer> pair =
                            list
                                    .stream()
                                    .map(m -> new ImmutablePair<>(m,
                                            calculateLevenshteinDistance(m.getMessage(), message)))
                                    .min(Comparator.comparingInt(p -> p.right))
                                    .orElse(new ImmutablePair<>(currentMessage, -1));

                    int currentMessageLenght = currentMessage.getMessage().length();
                    int resultMessageLength = pair.getKey().getMessage().length();

                    double border = currentMessageLenght > resultMessageLength ?
                            Math.sqrt(currentMessageLenght * 2) :
                            Math.sqrt(resultMessageLength * 2);
                    return pair.right != -1 && pair.right < border ? pair.left : currentMessage;
                }
        );
    }

    public int calculateLevenshteinDistance(String x, String y)
    {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++)
        {
            for (int j = 0; j <= y.length(); j++)
            {
                if (i == 0)
                {
                    dp[i][j] = j;
                }
                else if (j == 0)
                {
                    dp[i][j] = i;
                }
                else
                {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        int result = dp[x.length()][y.length()];
        return result;
    }

    private int costOfSubstitution(char a, char b)
    {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers)
    {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

}
