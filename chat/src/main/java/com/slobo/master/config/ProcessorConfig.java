package com.slobo.master.config;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class ProcessorConfig {

    @Bean
    public StanfordCoreNLP stanfordCoreNLP() {
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment",
                "ssplit.isOneSentence", "true",
                "tokenize.language", "en");

        return new StanfordCoreNLP(props);
    }

}
