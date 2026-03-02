package com.product_service.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class AiController {

    private final ChatClient chatClient;
    private final List<Message> conversation;

    public AiController(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.conversation = new ArrayList<>();
        final String systemMessage = """
                Suggest product and category information to users.
                If someone asks about something else, just say I don't know.
                """;
        final SystemMessage message = new SystemMessage(systemMessage);
        this.conversation.add(message);
    }

    @GetMapping("/information")
    public String suggestProductInformation(
            @RequestParam(
                    name = "message",
                    defaultValue = "Suggest a product information for users"
            ) String message
    ) {
        final Message userMessage = new UserMessage(message);
        this.conversation.add(userMessage);
        String modelResponse = this.chatClient
                .prompt()
                .messages(this.conversation)
                .call()
                .content();
        final Message assistantMessage = new AssistantMessage(modelResponse);
        this.conversation.add(assistantMessage);
        return modelResponse;
    }
}
