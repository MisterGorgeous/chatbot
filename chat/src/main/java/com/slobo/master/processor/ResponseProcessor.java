package com.slobo.master.processor;

import com.slobo.master.model.ChatMessage;
import com.slobo.master.service.ChatBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import sun.misc.Contended;

@Contended
public class ResponseProcessor {

    @Autowired
    @Qualifier("webChatBot")
    private ChatBot chatbot;

    @Autowired
    private CacheMessageProcessor cacheMessageProcessor;

    public void respond(ChatMessage chatMessage) throws Exception {
        cacheMessageProcessor.lookUpMessage(chatMessage.getContent());

        String chatBotResponse = respondChatBot(chatMessage);

        //cacheMessageProcessor.saveMessage();
    }

    private String respondChatBot(ChatMessage chatMessage) throws Exception {
        if (!chatbot.isChatBotConnected()) {
            initChatbot();
        }
        return chatbot.respond(chatMessage);
    }

    private void initChatbot() throws Exception {
        chatbot.connect();
        chatbot.connectToChatBotServer();
    }
}
