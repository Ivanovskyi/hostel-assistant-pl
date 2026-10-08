package com.example.tool;

import com.example.model.SearchRequest;
import com.example.model.SearchResponse;
import com.example.service.HostelSearchService;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HostelSearchTool {

    private final HostelSearchService hostelSearchService;

    public HostelSearchTool(HostelSearchService hostelSearchService) {
        this.hostelSearchService = hostelSearchService;
    }

    @Tool("""
        Search for hostels in Poland.

        Use this tool whenever the user asks to find,
        search for, recommend or compare hostels.

        The city must be provided.

        Include dates when the user provides them.

        Include the user's maximum budget when provided.
        Preserve the budget exactly as stated by the user,
        including the currency.

        Return only real search results from the external search service.
        Never invent hostels, prices, ratings or websites.

        The backend applies the final budget filter.
        """)
    public SearchResponse searchHostels(SearchRequest request) {
        return hostelSearchService.search(request);
    }
}
