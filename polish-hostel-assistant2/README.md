# Polish Hostel Assistant

An AI-powered hostel search assistant for Poland built with Quarkus and LangChain4j.

## Project Overview

This application provides a conversational interface for searching hostels in Polish cities. It uses AI to understand natural language queries and searches for real hostel information from trusted booking platforms (Hostelworld, Booking.com, Hostels.com).

## Tech Stack

- **Framework**: Quarkus 3.40.1
- **Language**: Java 25
- **AI/LLM**: LangChain4j with OpenAI integration (via OpenRouter)
- **Search API**: Tavily
- **REST**: Jakarta REST with Jackson JSON serialization
- **Build Tool**: Maven
- **Containerization**: Docker (JVM, native, and legacy-jar variants)

## Project Structure

```
src/main/java/com/example/
├── api/
│   └── ChatResource.java          # REST endpoint
├── ai/
│   └── HostelAssistant.java      # AI service with system prompt
├── tool/
│   └── HostelSearchTool.java     # LangChain4j tool
├── service/
│   ├── HostelSearchService.java   # Search business logic
│   └── HostelTextParser.java     # Text parsing utilities
├── client/
│   ├── TavilyClient.java          # Tavily API client
│   └── TavilyResponse.java        # Response models
├── model/
│   ├── ChatRequest.java           # Request model
│   ├── Hostel.java                # Hostel data model
│   ├── SearchRequest.java         # Search parameters
│   └── SearchResponse.java        # Search results
└── GreetingResource.java         # Sample endpoint
```

### Configuration

The application is configured in `src/main/resources/application.properties`:

- **OpenRouter**: Used for AI model access (configured as OpenAI-compatible endpoint)
- **Tavily**: Used for web search API
- **HTTP Port**: 8080 (default)

## How to Run

### Development Mode

Run with live coding enabled:

```bash
./mvnw clean quarkus:dev
```

The application will be available at `http://localhost:8080`

### Test the API

Send a chat request:

```bash
curl -i -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Find hostels in Wroclaw for up to 300 PLN per night"}'
```

## Additional Resources

- [Quarkus Documentation](https://quarkus.io/)
- [LangChain4j Quarkus Guide](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
- [Quarkus REST Guide](https://quarkus.io/guides/rest)
  