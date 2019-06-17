package com.slobo.master.processor;

import com.slobo.master.model.ProcessedUserMessage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UserMessagePreProcessorTest {
    // private String TEXT = "Maternity pay for new mothers is to rise by £1,400 as part of new proposals announced by the Trade and Industry Secretary Patricia Hewitt.";
    private String TEXT = "The West End is honouring its.";
    private String TEXT2 = "The West End is honouring its oil";
    private String TEXT3 = "The West End is honouring its oil and gold industry poorly.";

    @Test
    public void process() {
        UserMessagePreProcessor userMessagePreProcessor = new UserMessagePreProcessor();
        ProcessedUserMessage process = userMessagePreProcessor.process(TEXT);
        String message = process.getMessage();

        ProcessedUserMessage currentMessage = userMessagePreProcessor.process(message);

        ProcessedUserMessage saved1 = userMessagePreProcessor.process(TEXT2);
        ProcessedUserMessage saved2 = userMessagePreProcessor.process(TEXT3);

        List<ProcessedUserMessage> savedMessages = Arrays.asList(saved1, saved2);

        ProcessedUserMessage result = savedMessages.stream()
                .filter(userMessagePreProcessor.bySentimentDistance(currentMessage))
                .filter(userMessagePreProcessor.byPOSDistance(currentMessage))
                .collect(userMessagePreProcessor.toMessage(currentMessage));

        String message1 = result.getMessage();
    }
}
