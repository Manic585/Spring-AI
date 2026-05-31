# Spring AI Demo

A Spring Boot backend demonstrating production-oriented Spring AI integration — covering chat, structured output, embeddings, vector search (RAG), image generation/description, and audio transcription/synthesis — built with a layered architecture, centralised exception handling, and consistent API contracts.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.5 |
| AI Integration | Spring AI 1.0.1 |
| AI Provider | OpenAI (Chat, Embeddings, DALL-E, Whisper, TTS) |
| Validation | Jakarta Validation (`spring-boot-starter-validation`) |
| Observability | Spring Boot Actuator |
| Utilities | Lombok |
| Build | Maven |

## Prerequisites

- Java 21+
- Maven 3.8+
- An [OpenAI API key](https://platform.openai.com/account/api-keys)

## Setup

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd Spring-AI
   ```

2. **Set your OpenAI API key as an environment variable:**
   ```bash
   export OPENAI_API_KEY=sk-...
   ```
   > The key is read via `${OPENAI_API_KEY}` in `application.properties` — never hard-code secrets.

3. **Build and run:**
   ```bash
   mvn spring-boot:run
   ```

The server starts on `http://localhost:8080`.

On startup, `DataInitializer` loads `supermarket_categories.txt` into an in-memory `SimpleVectorStore` using token-based chunking (500 tokens, 200-token overlap). This index backs all RAG endpoints.

---

## Architecture

```
src/main/java/com/practice/springAI/
├── controller/          # Thin HTTP layer — validate input, delegate, wrap response
│   ├── OpenAiController.java
│   ├── MovieController.java
│   ├── AudioController.java
│   └── ImageGenController.java
├── service/             # Business logic and AI model interaction
│   ├── ChatService.java
│   ├── MovieService.java
│   ├── RagService.java
│   ├── AudioService.java
│   └── ImageService.java
├── dto/
│   ├── ApiResponse.java       # Consistent response envelope
│   └── SimilarityResponse.java
├── exception/
│   ├── SpringAiException.java        # Typed application exception
│   └── GlobalExceptionHandler.java   # @RestControllerAdvice
├── AppConfig.java         # VectorStore bean
├── DataInitializer.java   # Loads knowledge base on startup
├── Movie.java             # Structured output model
└── SpringAiApplication.java
```

**Key design decisions:**
- Controllers are free of business logic — they validate inputs (`@Validated`, `@NotBlank`, `@Size`) and delegate to services.
- All endpoints return `ApiResponse<T>` — a consistent envelope with `success`, `data`, `error`, and `timestamp` fields.
- `GlobalExceptionHandler` centralises error handling for validation failures, missing parameters, file size violations, and unexpected errors.
- Services own all AI interaction and translate AI SDK exceptions into typed `SpringAiException` with appropriate HTTP status codes.

---

## API Response Envelope

Every endpoint (except binary audio download) returns this structure:

```json
{
  "success": true,
  "data": "<response payload>",
  "timestamp": "2025-05-31T10:00:00Z"
}
```

On error:

```json
{
  "success": false,
  "error": "Required parameter missing: actor",
  "timestamp": "2025-05-31T10:00:00Z"
}
```

---

## API Reference

### Chat

#### `GET /chat/{message}`
Send a free-form message to the chat model.

| Parameter | Type | Location | Constraints |
|-----------|------|----------|-------------|
| `message` | String | Path | Required, max 1000 chars |

**Example:**
```
GET /chat/What is retrieval-augmented generation?
```
**Response:**
```json
{ "success": true, "data": "RAG is a technique that...", "timestamp": "..." }
```

---

#### `POST /chat/recommend`
Get movie recommendations using a structured prompt template.

| Parameter  | Type   | Location | Constraints |
|------------|--------|----------|-------------|
| `type`     | String | Query    | Required, not blank |
| `year`     | String | Query    | Required, not blank |
| `language` | String | Query    | Required, not blank |

**Example:**
```
POST /chat/recommend?type=thriller&year=2023&language=English
```
**Response:** `data` contains a numbered list of 5 movies with cast, runtime, and IMDB rating.

---

### Movies — Structured Output

These endpoints demonstrate Spring AI's `BeanOutputConverter` and `ListOutputConverter` for type-safe AI responses.

#### `GET /movie/movies`
Top 5 movie titles for an actor, as a plain list.

| Parameter | Type   | Constraints          |
|-----------|--------|----------------------|
| `actor`   | String | Required, max 100 chars |

```
GET /movie/movies?actor=Leonardo DiCaprio
```
```json
{ "success": true, "data": ["Inception", "The Revenant", "..."], "timestamp": "..." }
```

---

#### `GET /movie/movieJson`
Best movie for an actor, mapped to the `Movie` model.

| Parameter | Type   | Constraints          |
|-----------|--------|----------------------|
| `actor`   | String | Required, max 100 chars |

```
GET /movie/movieJson?actor=Tom Hanks
```
```json
{
  "success": true,
  "data": { "name": "Forrest Gump", "actor": "Tom Hanks", "director": "Robert Zemeckis", "year": 1994 },
  "timestamp": "..."
}
```

---

#### `GET /movie/moviesList`
Full filmography for an actor, as a list of `Movie` objects.

| Parameter | Type   | Constraints          |
|-----------|--------|----------------------|
| `actor`   | String | Required, max 100 chars |

```
GET /movie/moviesList?actor=Rajinikanth
```
```json
{
  "success": true,
  "data": [
    { "name": "Enthiran", "actor": "Rajinikanth", "director": "S. Shankar", "year": 2010 }
  ],
  "timestamp": "..."
}
```

---

### Embeddings & RAG

#### `POST /api/embedding`
Returns the embedding vector for a text using OpenAI's embedding model.

| Parameter | Type   | Constraints           |
|-----------|--------|-----------------------|
| `text`    | String | Required, max 5000 chars |

```
POST /api/embedding?text=Spring Boot is awesome
```
**Response:** `data` is a `float[]` — high-dimensional vector representation.

---

#### `POST /api/similarity`
Computes cosine similarity between two texts (range: −1 to 1, higher = more similar).

| Parameter | Type   | Constraints           |
|-----------|--------|-----------------------|
| `t1`      | String | Required, max 1000 chars |
| `t2`      | String | Required, max 1000 chars |

```
POST /api/similarity?t1=dog&t2=puppy
```
```json
{
  "success": true,
  "data": { "text1": "dog", "text2": "puppy", "similarityScore": 0.91 },
  "timestamp": "..."
}
```

---

#### `POST /api/product`
Semantic similarity search over the loaded supermarket knowledge base.

| Parameter | Type   | Constraints         |
|-----------|--------|---------------------|
| `text`    | String | Required, max 500 chars |

```
POST /api/product?text=healthy snacks for kids
```
**Response:** `data` is a list of matching `Document` chunks from the vector store.

---

#### `POST /api/ask`
RAG endpoint — retrieves relevant chunks from the vector store and uses them as context for the chat model.

| Parameter | Type   | Constraints          |
|-----------|--------|----------------------|
| `query`   | String | Required, max 1000 chars |

```
POST /api/ask?query=What dairy products are available?
```
**Response:** `data` is a grounded plain-text answer based on the supermarket categories document.

---

### Image

#### `GET /image/{query}`
Generates an HD 1024×1024 image via DALL-E and returns its URL.

| Parameter | Type   | Constraints           |
|-----------|--------|-----------------------|
| `query`   | String | Required, max 1000 chars |

```
GET /image/a futuristic city at sunset in natural style
```
```json
{ "success": true, "data": "https://oaidalleapi...", "timestamp": "..." }
```

---

#### `POST /image/describe`
Describes an uploaded image using GPT-4 Vision.

| Parameter | Type          | Location  | Constraints            |
|-----------|---------------|-----------|------------------------|
| `query`   | String        | Form-data | Required, max 500 chars |
| `file`    | MultipartFile | Form-data | JPEG, max 25 MB        |

```bash
curl -X POST http://localhost:8080/image/describe \
  -F "query=What objects are in this image?" \
  -F "file=@photo.jpg"
```
```json
{ "success": true, "data": "The image shows a...", "timestamp": "..." }
```

---

### Audio

#### `POST /api/speechToText`
Transcribes an audio file using OpenAI Whisper. Returns an SRT-formatted transcript.

| Parameter | Type          | Location  | Constraints   |
|-----------|---------------|-----------|---------------|
| `file`    | MultipartFile | Form-data | Max 25 MB     |

```bash
curl -X POST http://localhost:8080/api/speechToText \
  -F "file=@recording.mp3"
```
```json
{ "success": true, "data": "1\n00:00:00,000 --> 00:00:02,000\nHello, world.\n", "timestamp": "..." }
```

---

#### `POST /api/textToSpeech`
Converts text to speech using OpenAI TTS (voice: `nova`, speed: `0.75×`). Returns audio bytes as `audio/mpeg`.

| Parameter | Type   | Constraints           |
|-----------|--------|-----------------------|
| `text`    | String | Required, max 4096 chars |

```bash
curl -X POST "http://localhost:8080/api/textToSpeech?text=Hello+World" \
  --output speech.mp3
```
**Response:** Binary `audio/mpeg` download (`speech.mp3`).

---

## Domain Model

```java
// Movie.java
String name;
String actor;
String director;
int    year;
```

---

## Observability

Actuator endpoints are available at `/actuator`:

| Endpoint | URL |
|----------|-----|
| Health | `GET /actuator/health` |
| Info | `GET /actuator/info` |
| Metrics | `GET /actuator/metrics` |

---

## Error Handling

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Blank or oversized input | `400 Bad Request` | Validation message |
| Missing required parameter | `400 Bad Request` | Parameter name |
| File exceeds 25 MB | `413 Payload Too Large` | Size error message |
| OpenAI API failure | `503 Service Unavailable` | Generic error message |
| Unexpected server error | `500 Internal Server Error` | Generic error message |

All error responses follow the `ApiResponse` envelope with `success: false` and an `error` field.
