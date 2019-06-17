package com.slobo.master.service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.slobo.master.model.ChatMessage;

@Component("webChatBot")
public class WebChatBot implements ChatBot {
    private Logger logger = LogManager.getLogger(WebChatBot.class);
    @Autowired
    @Qualifier("pandorabotsChatterBot")
    private ChatterBot chatterBot;
    private ChatterBotSession session;
    private boolean isChatBotConnected;

    @Override
    public String respond(ChatMessage chatMessage) throws Exception {
        String response = session.think(chatMessage.getContent());
        logger.info("WebChatBot respond" + response);
        return response;
    }

    @Override
    public boolean isChatBotConnected() {
        return isChatBotConnected;
    }

    @Override
    public void connectToChatBotServer() {
        logger.info("WebChatBot connected to chatBot server");
        session = chatterBot.createSession();
        isChatBotConnected = true;
    }

}
