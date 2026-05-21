package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class TravelSearchClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper(); // IMPORTANT FIX

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    public TravelSearchClient(@Client("/") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Hostel> search(SearchRequest request) {

        List<Hostel> hostels = new ArrayList<>();

        try {
            String query = buildQuery(request);

            Map<String, Object> body = Map.of(
                    "api_key", tavilyApiKey,
                    "query", query,
                    "search_depth", "advanced",
                    "max_results", 10,
                    "include_answer", false,
                    "include_raw_content", false
            );

            HttpRequest<Map<String, Object>> httpRequest = HttpRequest
                    .POST("https://api.tavily.com/search", body)
                    .contentType(MediaType.APPLICATION_JSON_TYPE);

            String response = httpClient.toBlocking().retrieve(httpRequest);

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {

                for (JsonNode item : results) {

                    String title = item.has("title")
                            ? item.get("title").asText()
                            : "Unknown Hostel";

                    String url = item.has("url")
                            ? item.get("url").asText()
                            : "";

                    String content = item.has("content")
                            ? item.get("content").asText()
                            : "";

                    String snippet = item.has("snippet")
                            ? item.get("snippet").asText()
                            : content;

                    String fullText = title + " " + content + " " + snippet;

                    String hostelName = extractHostelName(title, content);
                    Double price = extractPrice(fullText);
                    Double rating = extractRating(fullText);
                    String currency = extractCurrency(fullText);
                    String description = extractDescription(content, snippet);

                    hostels.add(new Hostel(
                            hostelName,
                            request.city(),
                            price,
                            currency,
                            rating,
                            description,
                            url
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hostels;
    }

    private String buildQuery(SearchRequest request) {

        StringBuilder query = new StringBuilder();

        query.append("best hostels in ")
                .append(request.city())
                .append(" Poland with prices and reviews ");

        if (request.dates() != null && !request.dates().isBlank()) {
            query.append(request.dates()).append(" ");
        }

        if (request.budget() != null && !request.budget().isBlank()) {
            query.append(request.budget()).append(" ");
        }

        query.append("site:booking.com OR site:hostelworld.com OR site:hostels.com");

        return query.toString();
    }

    private String cleanTitle(String title) {

        return title
                .replace("| Booking.com", "")
                .replace("| Hostelworld", "")
                .replace("- Hostelworld", "")
                .trim();
    }

    private String extractHostelName(String title, String content) {
        // Try to extract a specific hostel name from title
        String cleaned = cleanTitle(title);
        
        // If title looks like a list page, try to extract from content
        if (cleaned.toLowerCase().contains("hostels in") || 
            cleaned.toLowerCase().contains("best hostels") ||
            cleaned.toLowerCase().contains("cheap hostels")) {
            
            // Look for hostel names in content (usually capitalized words followed by hostel)
            Pattern hostelPattern = Pattern.compile("\\b([A-Z][A-Za-z\\s]+?Hostel)\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = hostelPattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            // Fallback: return first meaningful part of title
            String[] parts = cleaned.split("\\|");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        
        return cleaned;
    }

    private Double extractPrice(String text) {
        // Try to extract price in various formats: $15, €20, 50 PLN, 30 EUR
        Pattern pricePattern = Pattern.compile("(?:\\$|€|£|PLN|EUR)\\s*(\\d+(?:\\.\\d+)?)|(\\d+(?:\\.\\d+)?)\\s*(?:PLN|EUR|€|$|£)");
        Matcher matcher = pricePattern.matcher(text);
        
        if (matcher.find()) {
            String priceStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            try {
                return Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Double extractRating(String text) {
        // Try to extract rating in formats: 8.5/10, 4.5 stars, rating: 9.0
        Pattern ratingPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(?:/\\s*10|stars?|rating)");
        Matcher matcher = ratingPattern.matcher(text);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String extractCurrency(String text) {
        if (text.contains("PLN") || text.toLowerCase().contains("zł")) {
            return "PLN";
        } else if (text.contains("€") || text.contains("EUR")) {
            return "EUR";
        } else if (text.contains("£")) {
            return "GBP";
        } else if (text.contains("$")) {
            return "USD";
        }
        return "EUR"; // Default to EUR for Poland
    }

    private String extractDescription(String content, String snippet) {
        // Use snippet if available and meaningful, otherwise use content
        String desc = snippet != null && !snippet.isBlank() ? snippet : content;
        
        // Clean up description
        desc = desc.replaceAll("\\s+", " ").trim();
        
        // Limit length
        if (desc.length() > 500) {
            desc = desc.substring(0, 497) + "...";
        }
        
        return desc;
    }
}