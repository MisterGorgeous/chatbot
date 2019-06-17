package com.slobo.master.processor;

import com.slobo.master.model.ChatMessage;
import com.slobo.master.model.ProcessedUserMessage;
import com.slobo.master.service.ChatBot;
import com.slobo.master.service.ChatBotSessionHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Component
public class ResponseProcessor {

    @Autowired
    @Qualifier("webChatBot")
    private ChatBot chatbot;
    private StompSession stompSession;
    @Autowired
    private WebSocketStompClient stompClient;
    @Autowired
    private ChatBotSessionHandler sessionHandler;
    @Autowired
    private CacheMessageProcessor cacheMessageProcessor;
    private boolean isConnectionToTheChatEstablished;
    private static String URL = "ws://localhost:8081/websocket";
    private Logger logger = LogManager.getLogger(ResponseProcessor.class);


    public void respond(ChatMessage chatMessage) throws Exception {
        if (!isConnectionToTheChatEstablished) {
            connectToTheChat();
        }

        ProcessedUserMessage message = cacheMessageProcessor.lookUpMessage(chatMessage.getContent());
        String response;

        if (StringUtils.isNotBlank(message.getResponseOnTheMessage())) {
            response = message.getResponseOnTheMessage();
        } else {
            response = respondChatBot(chatMessage);
            message.setResponseOnTheMessage(response);
            cacheMessageProcessor.saveMessage(message);
        }

        stompSession.send("/topic/public",
                new ChatMessage(ChatMessage.MessageType.CHAT, response, ChatMessage.CHATBOT));
    }

    private String respondChatBot(ChatMessage chatMessage) throws Exception {
        if (!chatbot.isChatBotConnected()) {
            initChatbot();
        }
        return chatbot.respond(chatMessage);
    }

    private void initChatbot() throws Exception {
        chatbot.connectToChatBotServer();
    }

    private void connectToTheChat() throws Exception {
        logger.info("WebChatBot connected to chatBot");
        stompSession = stompClient.connect(URL, sessionHandler).get();
        isConnectionToTheChatEstablished = true;
    }

}
