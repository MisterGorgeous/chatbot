package com.slobo.master.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import com.slobo.master.model.ChatMessage;
import com.slobo.master.service.ChatBot;

@Controller
public class ChatController {
    @Autowired
    private ChatBot chatbot;


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatbot.respond(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) throws ExecutionException, InterruptedException {
        if (!chatbot.isChatBotConnected()) {
            chatbot.connect();
        }

        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @MessageExceptionHandler
    @SendTo("/topic/errors")
    public ChatMessage handleExcpetion(Throwable exception) {
        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setContent("Error message.");
        errorMessage.setType(ChatMessage.MessageType.ERROR);
        return errorMessage;
    }

}
