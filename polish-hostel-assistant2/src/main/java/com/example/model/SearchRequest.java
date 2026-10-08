package com.example.model;

public record SearchRequest(
        String city,
        String dates,
        String budget
) {
}
