package com.practice.springAI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class OpenAiController {
    private ChatClient chatClient;

    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    public OpenAiController(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel){
        this.chatClient = ChatClient.create(openAiChatModel);
    }

    @GetMapping("/chat/{message}")
    public ResponseEntity<String> getResponse(@PathVariable String message){
        String response = chatClient
                .prompt(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/recommend")
    public ResponseEntity<String> getRecommendation(@RequestParam String type, @RequestParam String year, @RequestParam String language){

        String template = """
                I want to watch {type} movie in {language} which got released in the {year}.
                Suggest 5 movies for the requirement and give me the following in numbered list.
                1. Cast 
                2. Length time
                3. IMDB Ratings
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "type", type,
                "year", year,
                "language", language));
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        String response = chatResponse
                .getResult()
                .getOutput()
                .getText();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/embedding")
    public float[] getEmbeddings(@RequestParam String text){
        return embeddingModel.embed(text);
    }

    @PostMapping("/api/similarity")
    public double getSimilarity(@RequestParam String t1, @RequestParam String t2){
        float[] embedding1 = getEmbeddings(t1);
        float[] embedding2 = getEmbeddings(t2);

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i=0;i<embedding1.length;i++){
            dotProduct += embedding1[i]*embedding2[i];
            norm1 += Math.pow(embedding1[i], 2);
            norm2 += Math.pow(embedding2[i], 2);

        }
        return dotProduct * 100 / Math.sqrt(norm1) * Math.sqrt(norm2);
    }

    @PostMapping("/api/product")
    public List<Document> getProducts(@RequestParam String text){
        return vectorStore.similaritySearch(text);
        // return vectorStore.similaritySearch(SearchRequest.builder().query(text).topK(2).build());
    }

    @PostMapping("/api/ask")
    public String getUsingRag(@RequestParam String query){
        return chatClient
                .prompt(query)
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .content();
    }



}
