package com.example.api;

import com.example.ai.HostelAssistant;
import com.example.model.ChatRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource {

    private final HostelAssistant assistant;

    public ChatResource(HostelAssistant assistant) {
        this.assistant = assistant;
    }

    @POST
    @Path("/chat")
    public Response chat(ChatRequest request) {

        if (request == null
                || request.message() == null
                || request.message().isBlank()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Message must not be empty")
                    .build();
        }

        String answer = assistant.chat(request.message());

        return Response.ok(answer).build();
    }
}
