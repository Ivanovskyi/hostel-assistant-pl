package com.example;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;

import io.micronaut.http.annotation.Post;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.TaskExecutors;


@Controller("/api")
public class ChatController {

    private final PolishHostelAssistant assistant;

    public ChatController(PolishHostelAssistant assistant) {
        this.assistant = assistant;
    }

    @Serdeable
    public record ChatRequest(String message) {
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Post("/chat")
    public String chat(@Body ChatRequest req) {
        return assistant.chat(req.message());
    }
}