package com.slobo.master.processor;

import com.slobo.master.model.ProcessedUserMessage;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.SentimentAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UserPhrasePreProcessor {

    private StanfordCoreNLP pipeline;

    public UserPhrasePreProcessor() {
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment",
                "ssplit.isOneSentence", "true",
                "tokenize.language", "en");

        pipeline = new StanfordCoreNLP(props);


    }

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
                posStatistics.merge(pos, 1, (currentValue, newValue) -> currentValue + newValue);

            }
        }

        return new ProcessedUserMessage(lemmatizatedMessage.toString(), posStatistics, sentimentScore);
    }

}
