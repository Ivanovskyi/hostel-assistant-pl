package com.example;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Hostel(
        String name,
        String city,
        Double pricePerNight,
        String currency,
        Double rating,
        String description,
        String website
) {}