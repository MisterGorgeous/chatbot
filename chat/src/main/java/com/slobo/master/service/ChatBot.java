package com.slobo.master.service;

import com.slobo.master.model.ChatMessage;

public interface ChatBot
{
    void respond(ChatMessage chatMessage) throws Exception;

    boolean isChatBotConnected() throws Exception;

    void connect() throws Exception;

    void connectToChatBotServer() throws Exception;
}
