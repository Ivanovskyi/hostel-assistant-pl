# Polish Hostel Assistant

An AI-powered hostel search assistant for discovering hostels in Poland. Ask questions in plain language - "find cheap hostels in Krakow for next weekend" or "budget hostels in Warsaw under 50 PLN" - and the assistant searches the web in real-time to find current hostel options with prices, ratings, and booking links.

Built with Micronaut 4, LangChain4j, OpenAI, and Tavily Search API.

## How It Works

When a user asks a question, the AI assistant uses OpenAI's GPT-4o model to understand the intent and automatically calls the hostel search tool. The tool queries the Tavily Search API to find real-time hostel information from booking sites like Booking.com, Hostelworld, and Hostels.com. The AI then extracts and presents the top 5 cheapest hostels with information including names, prices, ratings, and direct website links.

## Architecture

- `PolishHostelAssistant` — LangChain4j `@AiService` handling conversation and tool orchestration
- `HostelSearchService` — `@Tool` method for searching hostels via Tavily API
- `TravelSearchClient` — HTTP client for Tavily Search API with regex-based parsing for prices, ratings, and descriptions
- `ChatController` — REST endpoint for chat interactions
- Data models: `Hostel`, `SearchRequest`, `SearchResponse`

## Quick Start

### 1. Set Your API Keys

```bash
export OPENAI_API_KEY=your-openai-key
export TAVILY_API_KEY=your-tavily-key
```

You can get a Tavily API key at https://tavily.com/

### 2. Run the Application

```bash
./mvnw mn:run
```

The app starts at `http://localhost:8080`.

## Building a Native Image

```bash
./mvnw package -Dpackaging=native-image
./target/polish-hostel-assistant
```

The native executable:
- Has a small footprint
- Starts in milliseconds
- Low memory consumption

## Example Queries

```bash
http POST http://localhost:8080/api/chat message="find cheap hostels in Krakow for next weekend"

http POST http://localhost:8080/api/chat message="budget hostels in Warsaw under 50 PLN"
```

Or with curl:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "I want affordable hostels in Wroclaw with good ratings"}'
```
