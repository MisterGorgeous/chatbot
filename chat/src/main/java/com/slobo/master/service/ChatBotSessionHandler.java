package com.slobo.master.service;

import java.lang.reflect.Type;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import com.slobo.master.model.ChatMessage;

@Component
public class ChatBotSessionHandler extends StompSessionHandlerAdapter
{

    private Logger logger = LogManager.getLogger(ChatBotSessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders)
    {
        session.subscribe("/topic/public", this);
        session.send("/app/chat.addUser", new ChatMessage(ChatMessage.MessageType.CHAT,
                "Hello.How can I help you?", ChatMessage.CHATBOT));
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
            Throwable exception)
    {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers)
    {
        return ChatMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload)
    {
        ChatMessage msg = (ChatMessage) payload;
        logger.info("Received : " + msg.getContent() + " from : " + msg.getSender());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable ex)
    {
        logger.error("Got an transport error", ex);
    }

}
