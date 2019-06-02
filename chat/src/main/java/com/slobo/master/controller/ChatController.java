package com.slobo.master.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.slobo.master.model.ChatMessage;
import com.slobo.master.service.ChatBot;

@Controller
public class ChatController
{
    @Autowired
    @Qualifier("webChatBot")
    private ChatBot chatbot;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) throws Exception
    {
        chatbot.respond(chatMessage);
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) throws Exception
    {
        if (!chatbot.isChatBotConnected())
        {
            chatbot.connect();
            chatbot.connectToChatBotServer();
        }

        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @MessageExceptionHandler
    @SendTo("/topic/errors")
    public ChatMessage handleExcpetion(Throwable exception)
    {
        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setContent("Error message.");
        errorMessage.setType(ChatMessage.MessageType.ERROR);
        return errorMessage;
    }

}
