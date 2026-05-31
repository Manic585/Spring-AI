# Spring AI Demo

A Spring Boot application that demonstrates key Spring AI capabilities using OpenAI models — chat, structured output, embeddings, vector search (RAG), image generation/description, and audio transcription/synthesis.

## Technologies

- Java 21
- Spring Boot 3.5.5
- Spring AI 1.0.1 (`spring-ai-starter-model-openai`)
- Spring AI Vector Store Advisors (`spring-ai-advisors-vector-store`)
- Lombok

## Prerequisites

- Java 21+
- Maven 3.6+
- An [OpenAI API key](https://platform.openai.com/account/api-keys)

## Setup

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd Spring-AI
   ```

2. **Set your OpenAI API key** in `src/main/resources/application.properties`:
   ```properties
   spring.ai.openai.api-key=<your-openai-api-key>
   ```

3. **Build and run:**
   ```bash
   mvn spring-boot:run
   ```

The app starts on `http://localhost:8080`.

> On startup, `DataInitializer` loads `supermarket_categories.txt` into an in-memory vector store using token-based chunking (500 tokens, 200 overlap). This powers the RAG endpoints.

---

## API Reference

### Chat

#### `GET /chat/{message}`
Send a free-form message to the OpenAI chat model.

| Parameter | Type | Location | Description |
|-----------|------|----------|-------------|
| `message` | String | Path | The message to send |

**Example:**
```
GET /chat/What is the capital of France?
```
**Response:** Plain text answer from the model.

---

#### `POST /chat/recommend`
Get movie recommendations based on genre, year, and language using a prompt template.

| Parameter  | Type   | Location | Description              |
|------------|--------|----------|--------------------------|
| `type`     | String | Query    | Movie genre (e.g. `action`) |
| `year`     | String | Query    | Release year (e.g. `2022`) |
| `language` | String | Query    | Movie language (e.g. `Tamil`) |

**Example:**
```
POST /chat/recommend?type=thriller&year=2023&language=English
```
**Response:** Numbered list of 5 movies with cast, runtime, and IMDB rating.

---

### Movies (Structured Output)

#### `GET /movie/movies`
Returns the top 5 movies for an actor as a plain list.

| Parameter | Type   | Location | Description      |
|-----------|--------|----------|------------------|
| `actor`   | String | Query    | Actor's name     |

**Example:**
```
GET /movie/movies?actor=Leonardo DiCaprio
```
**Response:**
```json
["Inception", "The Revenant", "Interstellar", "The Wolf of Wall Street", "Titanic"]
```

---

#### `GET /movie/movieJson`
Returns the best movie of an actor as a structured JSON object.

| Parameter | Type   | Location | Description  |
|-----------|--------|----------|--------------|
| `actor`   | String | Query    | Actor's name |

**Example:**
```
GET /movie/movieJson?actor=Tom Hanks
```
**Response:**
```json
{
  "name": "Forrest Gump",
  "actor": "Tom Hanks",
  "director": "Robert Zemeckis",
  "year": 1994
}
```

---

#### `GET /movie/moviesList`
Returns a list of movies for an actor, each as a structured JSON object.

| Parameter | Type   | Location | Description  |
|-----------|--------|----------|--------------|
| `actor`   | String | Query    | Actor's name |

**Example:**
```
GET /movie/moviesList?actor=Rajinikanth
```
**Response:**
```json
[
  { "name": "Enthiran", "actor": "Rajinikanth", "director": "S. Shankar", "year": 2010 },
  { "name": "Kabali",   "actor": "Rajinikanth", "director": "Pa. Ranjith", "year": 2016 }
]
```

---

### Embeddings & Vector Search

#### `POST /api/embedding`
Returns the embedding vector for a given text using `text-embedding-ada-002`.

| Parameter | Type   | Location | Description        |
|-----------|--------|----------|--------------------|
| `text`    | String | Query    | Text to embed      |

**Example:**
```
POST /api/embedding?text=Spring Boot is awesome
```
**Response:** `float[]` — a high-dimensional embedding vector.

---

#### `POST /api/similarity`
Computes the cosine similarity score between two texts.

| Parameter | Type   | Location | Description   |
|-----------|--------|----------|---------------|
| `t1`      | String | Query    | First text    |
| `t2`      | String | Query    | Second text   |

**Example:**
```
POST /api/similarity?t1=dog&t2=puppy
```
**Response:** A `double` score. Higher values indicate greater semantic similarity.

---

#### `POST /api/product`
Performs a vector similarity search over the loaded supermarket product catalog.

| Parameter | Type   | Location | Description            |
|-----------|--------|----------|------------------------|
| `text`    | String | Query    | Natural language query |

**Example:**
```
POST /api/product?text=healthy snacks for kids
```
**Response:** A list of matching `Document` objects from the vector store.

---

#### `POST /api/ask`
RAG (Retrieval-Augmented Generation) endpoint. Retrieves relevant chunks from the supermarket vector store and passes them as context to the chat model.

| Parameter | Type   | Location | Description              |
|-----------|--------|----------|--------------------------|
| `query`   | String | Query    | Question to answer       |

**Example:**
```
POST /api/ask?query=What dairy products are available?
```
**Response:** Plain text answer grounded in the supermarket categories document.

---

### Image

#### `GET /image/{query}`
Generates an HD image using DALL-E and returns its URL.

| Parameter | Type   | Location | Description                     |
|-----------|--------|----------|---------------------------------|
| `query`   | String | Path     | Image description / prompt      |

**Example:**
```
GET /image/a futuristic city at sunset in natural style
```
**Response:** A URL string pointing to the generated 1024×1024 HD image.

---

#### `POST /image/describe`
Accepts an image file and a question, then returns a natural language description using GPT-4 Vision.

| Parameter | Type          | Location    | Description                        |
|-----------|---------------|-------------|------------------------------------|
| `query`   | String        | Form-data   | Question or instruction about the image |
| `file`    | MultipartFile | Form-data   | JPEG image to analyze              |

**Example (curl):**
```bash
curl -X POST http://localhost:8080/image/describe \
  -F "query=What objects are in this image?" \
  -F "file=@photo.jpg"
```
**Response:** Plain text description/answer based on image content.

---

### Audio

#### `POST /api/speechToText`
Transcribes an audio file using OpenAI Whisper. Returns an SRT-formatted transcript.

| Parameter | Type          | Location  | Description           |
|-----------|---------------|-----------|-----------------------|
| `file`    | MultipartFile | Form-data | Audio file to transcribe |

**Example (curl):**
```bash
curl -X POST http://localhost:8080/api/speechToText \
  -F "file=@recording.mp3"
```
**Response:** SRT-formatted subtitles string.

---

#### `POST /api/textToSpeech`
Converts text to speech using OpenAI TTS (voice: `nova`, speed: `0.75x`). Returns raw audio bytes.

| Parameter | Type   | Location | Description          |
|-----------|--------|----------|----------------------|
| `text`    | String | Query    | Text to speak aloud  |

**Example (curl):**
```bash
curl -X POST "http://localhost:8080/api/textToSpeech?text=Hello+World" \
  --output speech.mp3
```
**Response:** Binary audio data (`byte[]`).

---

## Movie Model

```json
{
  "name":     "string",
  "actor":    "string",
  "director": "string",
  "year":     "integer"
}
```

## Project Structure

```
src/main/java/com/practice/springAI/
├── SpringAiApplication.java          # Entry point
├── AppConfig.java                    # Bean configuration
├── DataInitializer.java              # Loads supermarket_categories.txt into vector store on startup
├── Movie.java                        # Structured output model
└── controller/
    ├── OpenAiController.java         # Chat, embeddings, vector search, RAG
    ├── MovieController.java          # Structured movie output endpoints
    ├── AudioController.java          # Speech-to-text and text-to-speech
    └── ImageGenController.java       # Image generation and description

src/main/resources/
├── application.properties
└── supermarket_categories.txt        # Knowledge base for RAG
```
