package com.practice.springAI.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarityResponse {
    private final String text1;
    private final String text2;
    private final double similarityScore;
}
