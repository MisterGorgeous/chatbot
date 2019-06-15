package com.slobo.master.processor;

import com.slobo.master.model.ProcessedUserMessage;
import org.junit.Test;

public class UserPhrasePreProcessorTest {
   // private String TEXT = "Maternity pay for new mothers is to rise by £1,400 as part of new proposals announced by the Trade and Industry Secretary Patricia Hewitt.";
    private String TEXT = "The West End is honouring its finest stars and shows at the Evening Standard Theatre Awards in London on Monday.";

    @Test
    public void process() {
        UserPhrasePreProcessor preProcessor = new UserPhrasePreProcessor();
        ProcessedUserMessage process = preProcessor.process(TEXT);
        String message = process.getMessage();
    }
}
