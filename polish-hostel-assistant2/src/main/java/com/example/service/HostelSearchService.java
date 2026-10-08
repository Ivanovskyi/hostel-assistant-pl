package com.example.service;

import com.example.client.TavilyClient;
import com.example.client.TavilyResponse;
import com.example.model.Hostel;
import com.example.model.SearchRequest;
import com.example.model.SearchResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class HostelSearchService {

    private static final Logger LOG = Logger.getLogger(HostelSearchService.class);

    private final TavilyClient tavilyClient;
    private final HostelTextParser textParser;

    @ConfigProperty(name = "tavily.api-key")
    String tavilyApiKey;

    public HostelSearchService(@RestClient TavilyClient tavilyClient, HostelTextParser textParser) {
        this.tavilyClient = tavilyClient;
        this.textParser = textParser;
    }

    public SearchResponse search(SearchRequest request) {

        validateRequest(request);

        String query = buildQuery(request);

        LOG.infof(
                "Searching hostels: city=%s, dates=%s, budget=%s",
                request.city(),
                request.dates(),
                request.budget()
        );

        TavilyClient.TavilySearchRequest tavilyRequest =
                new TavilyClient.TavilySearchRequest(
                        tavilyApiKey,
                        query,
                        "advanced",
                        10,
                        false,
                        false
                );

        TavilyResponse response = tavilyClient.search(tavilyRequest);

        if (response == null || response.results() == null) {

            LOG.warn("Tavily returned no results");

            return new SearchResponse(List.of());
        }

        List<Hostel> hostels = new ArrayList<>();

        for (TavilyResponse.TavilyResult result : response.results()) {

            if (result == null) {
                continue;
            }

            String title = safe(result.title());
            String content = safe(result.content());
            String url = safe(result.url());

            if (!isAllowedSource(url)) {
                continue;
            }

            String fullText = title + " " + content;

            if (textParser.isListPage(title)) {
                LOG.infof("Skipping list page: %s", title);
                continue;
            }

            if (!containsCity(fullText, request.city())) {
                LOG.infof("Skipping result unrelated to city %s: %s", request.city(), title);
                continue;
            }

            String hostelName = textParser.extractHostelName(title, content);

            if (hostelName == null || hostelName.isBlank()) {
                LOG.infof("Skipping result without hostel name: %s", title);
                continue;
            }

            if (textParser.isListPage(hostelName)) {
                continue;
            }

            Double price = textParser.extractPrice(fullText);

            HostelTextParser.Rating rating = textParser.extractRating(fullText);

            String currency = price != null ? textParser.extractCurrency(fullText) : null;

            String description = textParser.extractDescription(content);

            Hostel hostel = new Hostel(
                            cleanHostelName(hostelName),
                            request.city(),
                            price,
                            currency,
                            rating.value(),
                            rating.scale(),
                            description,
                            url);

            if (isDuplicate(hostels, hostel)) {
                LOG.infof("Skipping duplicate hostel: %s", hostel.name());
                continue;
            }

            hostels.add(hostel);
        }

        List<Hostel> results = hostels.stream().limit(5).toList();

        LOG.infof("Returning %d hostel results", results.size());

        return new SearchResponse(results);
    }

    private void validateRequest(SearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (request.city() == null || request.city().isBlank()) {

            throw new IllegalArgumentException(
                    "City is required"
            );
        }
    }

    private String buildQuery(SearchRequest request) {

        StringBuilder query = new StringBuilder();

        query.append("specific hostel properties in ");

        query.append(request.city()).append(", Poland ");

        if (request.dates() != null && !request.dates().isBlank()) {

            query.append("for dates ").append(request.dates()).append(" ");
        }

        if (request.budget() != null && !request.budget().isBlank()) {

            query.append("maximum ").append(request.budget()).append(" PLN per night ");
        }

        query.append(
                "current accommodation price per night in PLN "
                        + "actual hostel accommodation price "
                        + "guest rating "
                        + "exclude deposits taxes and fees "
        );

        query.append(
                "(site:hostelworld.com "
                        + "OR site:booking.com "
                        + "OR site:hostels.com)"
        );

        return query.toString();
    }

    private boolean isAllowedSource(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String normalized = url.toLowerCase();

        return normalized.contains("hostelworld.com")
                || normalized.contains("booking.com")
                || normalized.contains("hostels.com");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean containsCity(String text, String city) {
        if (text == null || text.isBlank() || city == null || city.isBlank()) {
            return false;
        }

        String normalizedText = text.toLowerCase();

        String normalizedCity = city.toLowerCase();

        return normalizedText.contains(normalizedCity);
    }

    private boolean isDuplicate(List<Hostel> hostels, Hostel candidate) {
        return hostels.stream().anyMatch(existing -> sameHostel(existing, candidate));
    }

    private boolean sameHostel(Hostel first, Hostel second) {
        if (first == null || second == null) {
            return false;
        }

        String firstName = normalizeName(first.name());

        String secondName = normalizeName(second.name());

        return firstName.equals(secondName);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.toLowerCase().replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    private String cleanHostelName(String name) {
        if (name == null) {
            return "";
        }

        return name.replaceAll("\\s+", " ").replaceAll("\\s+Hostel$", " Hostel").trim();
    }
}
