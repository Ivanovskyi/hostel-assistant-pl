package com.example.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/search")
@RegisterRestClient(configKey = "tavily")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TavilyClient {

    @POST
    TavilyResponse search(TavilySearchRequest request);

    record TavilySearchRequest(
            String api_key,
            String query,
            String search_depth,
            Integer max_results,
            Boolean include_answer,
            Boolean include_raw_content
    ) {
    }
}
