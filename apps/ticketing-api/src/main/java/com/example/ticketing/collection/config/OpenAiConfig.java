package com.example.ticketing.collection.config;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.springboot.OpenAIClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Bean
    public OpenAIClientCustomizer openAIClientCustomizer() {
        return builder -> builder
                .maxRetries(3)
                .responseValidation(true);
    }

    @Bean
    public AiChatClient openAiChatClient(OpenAIClient client) {
        return prompt -> {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .addUserMessage(prompt)
                    .build();

            return client.chat().completions().create(params)
                    .choices().get(0)
                    .message().content().orElse("");
        };
    }
}
