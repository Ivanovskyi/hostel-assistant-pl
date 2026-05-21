package com.example;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class HostelSearchService {

    private final TravelSearchClient travelSearchClient;

    public HostelSearchService(TravelSearchClient travelSearchClient) {
        this.travelSearchClient = travelSearchClient;
    }

    @Tool("Search for cheap hostels in Poland with detailed information including prices, ratings, and descriptions")
    public SearchResponse searchHostels(SearchRequest request) {

        System.out.println("TOOL EXECUTED: Searching for hostels in " + request.city());

        List<Hostel> hostels = travelSearchClient.search(request);

        // Filter out results without hostel names and limit to top 3
        List<Hostel> results = hostels.stream()
                .filter(h -> h.name() != null && !h.name().isBlank())
                .filter(h -> !h.name().toLowerCase().contains("hostels in"))
                .limit(5)
                .toList();

        System.out.println("Found " + results.size() + " hostels");

        return new SearchResponse(results);
    }
}