package com.slobo.master.config;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfig {

    @Bean
    public ChatterBot cleverChatterBot() throws Exception {
        ChatterBotFactory factory = new ChatterBotFactory();

        return factory.create(ChatterBotType.CLEVERBOT);
    }

    @Bean
    public ChatterBot pandorabotsChatterBot() throws Exception {
        ChatterBotFactory factory = new ChatterBotFactory();

        return factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
    }

}
