package com.example;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SearchRequest(
        String city,
        String dates,
        String budget
) {
}