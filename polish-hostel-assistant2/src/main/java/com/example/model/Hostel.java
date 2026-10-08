package com.example.model;

public record Hostel(
        String name,
        String city,
        Double pricePerNight,
        String currency,
        Double rating,
        String ratingScale,
        String description,
        String website
) {
}
