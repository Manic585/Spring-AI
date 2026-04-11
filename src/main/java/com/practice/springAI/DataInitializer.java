package com.practice.springAI;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private VectorStore vectorStore;

    @PostConstruct
    public void initData(){
        TextReader textReader = new TextReader(new ClassPathResource("supermarket_categories.txt"));

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(500, 200, 5, 5000, true);
        List<Document> documents = tokenTextSplitter.split(textReader.get());

        vectorStore.add(documents);
    }
}
