package com.example;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record SearchResponse(
        List<Hostel> hostels
) {
}