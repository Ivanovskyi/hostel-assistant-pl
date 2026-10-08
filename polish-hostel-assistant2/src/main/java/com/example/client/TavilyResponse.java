package com.example.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilyResponse(List<TavilyResult> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TavilyResult(
            String title,
            String url,
            String content
    ) {
    }
}