package com.example.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class HostelTextParser {

    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(?:€|EUR|PLN|zł|\\$|£)\\s*(\\d+(?:[.,]\\d+)?)"
                    + "|(\\d+(?:[.,]\\d+)?)\\s*(?:€|EUR|PLN|zł|\\$|£)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RATING_PATTERN = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*/\\s*(5|10)"
                    + "|(?:rating|rated|score|guest rating)"
                    + "\\s*[:]?\\s*(\\d+(?:[.,]\\d+)?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HOSTEL_PATTERN = Pattern.compile(
            "\\b([\\p{L}0-9&'’.-]+(?:\\s+[\\p{L}0-9&'’.-]+){0,6}\\s+Hostel)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public String extractHostelName(String title, String content) {
        String cleanedTitle = cleanTitle(title);

        if (!looksLikeListPage(cleanedTitle)) {
            return cleanedTitle;
        }

        Matcher matcher = HOSTEL_PATTERN.matcher(content == null ? "" : content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    public Double extractPrice(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String cleaned = text.replaceAll("(?i)(?:fully\\s+returnable\\s+)?deposit[^.]*", " ")
                .replaceAll("(?i)locker\\s+key[^.]*", " ");

        Matcher matcher = PRICE_PATTERN.matcher(cleaned);

        if (!matcher.find()) {
            return null;
        }

        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Rating extractRating(String text) {
        if (text == null || text.isBlank()) {
            return new Rating(null, null);
        }

        Matcher matcher = RATING_PATTERN.matcher(text);

        if (!matcher.find()) {
            return new Rating(null, null);
        }

        try {
            if (matcher.group(1) != null) {
                return new Rating(Double.parseDouble(matcher.group(1).replace(',', '.')),
                        "/" + matcher.group(2)
                );
            }

            return new Rating(
                    Double.parseDouble(matcher.group(3).replace(',', '.')), "/10");

        } catch (NumberFormatException e) {
            return new Rating(null, null);
        }
    }

    public String extractCurrency(String text) {
        if (text == null) {
            return null;
        }

        String normalized = text.toLowerCase(Locale.ROOT);

        if (normalized.contains("pln") || normalized.contains("zł")) {
            return "PLN";
        }

        if (normalized.contains("eur") || normalized.contains("€")) {
            return "EUR";
        }

        if (normalized.contains("gbp") || normalized.contains("£")) {
            return "GBP";
        }

        if (normalized.contains("usd") || normalized.contains("$")) {
            return "USD";
        }

        return null;
    }

    public String extractDescription(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String description = content.replaceAll("\\s+", " ").trim();

        if (description.length() <= 500) {
            return description;
        }

        return description.substring(0, 497) + "...";
    }

    public boolean isListPage(String name) {
        return looksLikeListPage(name);
    }

    private String cleanTitle(String title) {
        if (title == null) {
            return "";
        }

        return title
                .replaceAll("\\s*[|\\-]\\s*Hostelworld\\s*$", "")
                .replaceAll("\\s*\\|\\s*Booking\\.com\\s*$", "")
                .replaceAll("\\s*\\|\\s*Hostels\\.com\\s*$", "")
                .trim();
    }

    private boolean looksLikeListPage(String name) {
        if (name == null) {
            return true;
        }

        String normalized = name.toLowerCase(Locale.ROOT);

        return normalized.contains("hostels in")
                || normalized.contains("best hostels")
                || normalized.contains("cheap hostels")
                || normalized.contains("top hostels")
                || normalized.contains("hostel guide")
                || normalized.contains("hostel list")
                || normalized.contains("cheap hotels")
                || normalized.contains("hotels near")
                || normalized.contains("hotels in")
                || normalized.contains("hostels near")
                || normalized.contains("hostels in wroc");
    }

    public record Rating(Double value, String scale) {}
}
