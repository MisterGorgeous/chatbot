package com.slobo.master.service;

import com.slobo.master.controller.ChatController;
import com.slobo.master.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ChatBotService {
    @Autowired
    private ChatController chatController;

    //@PostConstruct
    public void sendMessage() {
        ChatMessage message = new ChatMessage();
        message.setSender("ChatBot");
        message.setType(ChatMessage.MessageType.CHAT);
        message.setContent("Hello world");
        chatController.sendMessage(message);
    }


}
