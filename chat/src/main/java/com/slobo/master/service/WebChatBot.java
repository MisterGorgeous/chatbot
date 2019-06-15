package com.slobo.master.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

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
    private StompSession stompSession;
    @Autowired
    private WebSocketStompClient stompClient;
    @Autowired
    private ChatBotSessionHandler sessionHandler;
    private static String URL = "ws://localhost:8081/websocket";

    @Override
    public String respond(ChatMessage chatMessage) throws Exception {
        String response = session.think(chatMessage.getContent());

        logger.info("WebChatBot respond" + response);

        stompSession.send("/topic/public",
                new ChatMessage(ChatMessage.MessageType.CHAT, response, ChatMessage.CHATBOT));

        return response;
    }

    @Override
    public boolean isChatBotConnected() throws Exception {
        return isChatBotConnected;
    }

    @Override
    public void connect() throws Exception {
        logger.info("WebChatBot connected to chatBot");
        stompSession = stompClient.connect(URL, sessionHandler).get();
        isChatBotConnected = true;
    }

    @Override
    public void connectToChatBotServer() throws Exception {
        logger.info("WebChatBot connected to chatBot server");
        session = chatterBot.createSession();
        isChatBotConnected = true;
    }

}
