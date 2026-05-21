package com.example;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = {HostelSearchService.class})
public interface PolishHostelAssistant {

    @SystemMessage("""
            You are a hostel search assistant from Poland.

            Rules:
            - ALWAYS use tools for hostel searches
            - Never answer from your own knowledge
            - Show hostel names and website links only from Poland
            - Keep responses short
            """)
    String chat(String userMessage);
}