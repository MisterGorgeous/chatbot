package com.slobo.master.service;

import com.slobo.master.model.ChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;

@Component
public class ChatBot {
    private Logger logger = LogManager.getLogger(ChatBot.class);
    @Autowired
    private ChatBotSessionHandler sessionHandler;
    @Autowired
    private WebSocketStompClient stompClient;
    private static String URL = "ws://localhost:8081/websocket";
    private boolean chatBotConnected = false;
    private StompSession stompSession;

    public void connect() throws ExecutionException, InterruptedException {
        stompSession = stompClient.connect(URL, sessionHandler).get();
        /*stompSession.subscribe("/topic/public", sessionHandler);
         */
        chatBotConnected = true;
    }

    public boolean isChatBotConnected() {
        return chatBotConnected;
    }

    public void setChatBotConnected(boolean chatBotConnected) {
        this.chatBotConnected = chatBotConnected;
    }

    public StompSession getStompSession() {
        return stompSession;
    }

    public void setStompSession(StompSession stompSession) {
        this.stompSession = stompSession;
    }

    @Async("chatTasksExecutor")
    public void respond(ChatMessage chatMessage) {
        if (chatBotConnected) {
            //get chat bot anwer
            stompSession.send("/topic/public",
                    new ChatMessage(ChatMessage.MessageType.CHAT, "Reasonable answer.", ChatMessage.CHATBOT));
        } else {
            logger.info("ChatBot isn't connected.Can't produce an answer.");
            stompSession.send("/chat.sendMessage",
                    new ChatMessage(ChatMessage.MessageType.CHAT, "Sorry. I am not ready to answer.", ChatMessage.CHATBOT));
        }
    }
}
