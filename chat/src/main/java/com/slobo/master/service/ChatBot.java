package com.slobo.master.service;

import com.slobo.master.model.ChatMessage;

public interface ChatBot
{
    String respond(ChatMessage chatMessage) throws Exception;

    boolean isChatBotConnected() throws Exception;

    void connectToChatBotServer() throws Exception;
}
