package com.slobo.master.service;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.slobo.master.model.ChatMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

@Component("shellChatBot")
public class ShellChatBot implements ChatBot {
    private Logger logger = LogManager.getLogger(ShellChatBot.class);
    @Autowired
    private ChatBotSessionHandler sessionHandler;
    @Autowired
    private WebSocketStompClient stompClient;
    private static String URL = "ws://localhost:8081/websocket";
    private boolean chatBotConnected = false;
    private boolean chatBotStarted = false;
    private StompSession stompSession;
    private Session session;
    private ChannelShell channel;

    private String host = "138.68.237.229";
    private String user = "root";
    private String password = "chatbot123";
    private String homeCommand = "cd home";
    private String lsCommand = "ls";
    private String startChatBotCommand = "java -jar cb_virtDialog.jar";

    public void connect() throws ExecutionException, InterruptedException {
        stompSession = stompClient.connect(URL, sessionHandler).get();
        chatBotConnected = true;
    }

    @Async("chatTasksExecutor")
    public String respond(ChatMessage chatMessage) throws IOException, JSchException {
        String chatbotAnswer = "Sorry. I am not ready to answer.";
        if (chatBotConnected && chatBotStarted) {
            chatbotAnswer = executeCommand("Enter your response or query > " + chatMessage.getContent());
            stompSession.send("/topic/public",
                    new ChatMessage(ChatMessage.MessageType.CHAT, chatbotAnswer, ChatMessage.CHATBOT));
        } else {
            logger.info("ChatBot can't produce an answer.");
            stompSession.send("/chat.sendMessage",
                    new ChatMessage(ChatMessage.MessageType.CHAT, chatbotAnswer, ChatMessage.CHATBOT));
        }
        return chatbotAnswer;
    }

    public void connectToChatBotServer() throws JSchException, IOException {
        logger.info("Connect to 138.68.237.229");

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig(config);
        session.connect();

        //chanel
        channel = (ChannelShell) session.openChannel("shell");
        channel.connect();

        logger.info("Connected to 138.68.237.229");

        String lsResult = executeCommand(lsCommand);

        logger.info("result" + lsResult);

        String homeCommandResult = executeCommand(homeCommand);

        logger.info(homeCommandResult);

        lsResult = executeCommand(lsCommand);

        logger.info(lsResult);

        String startChatBotCommandResult = executeCommand(startChatBotCommand);

        logger.info(startChatBotCommandResult);

        chatBotStarted = true;
    }

    public String executeCommand(String command) throws JSchException, IOException {
        if (session == null || channel == null) {
            throw new IllegalStateException("Couldn't execute command." + command);
        }

        logger.info("Execute " + command);

        PrintStream out = new PrintStream(channel.getOutputStream());
        out.println(command);
        out.flush();

        //String commandResult = IOUtils.toString(in, StandardCharsets.UTF_8.name());
        InputStream in = channel.getInputStream();
        StringBuilder result = new StringBuilder();
        byte[] tmp = new byte[1024];
        String line = "";
        int breakCounter = 0;
        while (true) {
            System.out.println("exit-status: ");
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                line = new String(tmp, 0, i);
                logger.info("Read " + line);
                result.append(line);
            }

            if (breakCounter > 10) {
                break;
            }

            if (line.contains("logout")) {
                break;
            }

            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
                breakCounter++;
            } catch (Exception ee) {
            }
        }

        logger.info("Result:" + result.toString());

        return result.toString();
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

    @PreDestroy
    public void finishUp() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

}
