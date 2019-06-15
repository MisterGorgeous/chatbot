package com.slobo.master.processor;

import com.slobo.master.model.ProcessedUserMessage;
import com.slobo.master.repository.UserMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class CacheMessageProcessor {


    @Autowired
    private UserMessagePreProcessor userMessagePreProcessor;
    @Autowired
    private UserMessageRepository userMessageRepository;

    public ProcessedUserMessage lookUpMessage(String message) {
        ProcessedUserMessage currentMessage = userMessagePreProcessor.process(message);

        List<ProcessedUserMessage> savedMessages = userMessageRepository.findAll();

        savedMessages.stream()
                .filter(userMessagePreProcessor.bySentimentDistance(currentMessage))
                .filter(userMessagePreProcessor.byPOSDistance(currentMessage))
                .collect(userMessagePreProcessor.toMessage(currentMessage));


    }


}
