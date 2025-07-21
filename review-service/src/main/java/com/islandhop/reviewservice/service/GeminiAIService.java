package com.islandhop.reviewservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.islandhop.reviewservice.dto.AIAnalysisResult;
import com.islandhop.reviewservice.enums.ReviewStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    public GeminiAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AIAnalysisResult analyzeReview(String reviewContent, double confidenceThreshold) {
        try {
            String prompt = buildAnalysisPrompt(reviewContent);
            log.info("[AI] Sending review to Gemini AI for moderation. Content: {}", reviewContent);
            String response = callGeminiAPI(prompt);
            log.info("[AI] Gemini API raw response: {}", response);
            AIAnalysisResult result = parseResponse(response, confidenceThreshold);
            log.info("[AI] Moderation result: status={}, confidenceScore={}, analysis={}, isConfident={}",
                    result.getRecommendedStatus(), result.getConfidenceScore(), result.getAnalysis(), result.isConfident());
            return result;
        } catch (Exception e) {
            log.error("[AI] Error analyzing review with Gemini AI: {}", e.getMessage(), e);
            // Return default result for manual review when AI fails
            return AIAnalysisResult.builder()
                    .recommendedStatus(ReviewStatus.TO_SUPPORT_AGENTS)
                    .confidenceScore(0.0)
                    .analysis("AI analysis failed: " + e.getMessage())
                    .isConfident(false)
                    .build();
        }
    }

    private String buildAnalysisPrompt(String reviewContent) {
        return String.format("""
            Analyze this review for appropriateness and safety. Consider the following criteria:
            1. Inappropriate language, hate speech, or offensive content
            2. Spam or promotional content
            3. Personal attacks or harassment
            4. False or misleading information
            5. Overall tone and constructiveness
            
            Review content: "%s"
            
            Please respond with a JSON object containing:
            {
                "status": "APPROVED" or "REJECTED",
                "confidence": [0.0 to 1.0],
                "reasoning": "Brief explanation of your decision"
            }
            
            Only return the JSON object, no additional text.
            """, reviewContent);
    }

    private String callGeminiAPI(String prompt) {
        try {
            log.info("[AI] Calling Gemini API with prompt: {}", prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ));

            String url = apiUrl + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );

            log.info("[AI] Gemini API HTTP status: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("[AI] Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private AIAnalysisResult parseResponse(String response, double confidenceThreshold) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode candidates = jsonResponse.get("candidates");
            
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).get("text").asText();
                        return parseAIDecision(text, confidenceThreshold);
                    }
                }
            }

            // If parsing fails, return default for manual review
            return AIAnalysisResult.builder()
                    .recommendedStatus(ReviewStatus.TO_SUPPORT_AGENTS)
                    .confidenceScore(0.0)
                    .analysis("Unable to parse AI response")
                    .isConfident(false)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return AIAnalysisResult.builder()
                    .recommendedStatus(ReviewStatus.TO_SUPPORT_AGENTS)
                    .confidenceScore(0.0)
                    .analysis("Error parsing AI response: " + e.getMessage())
                    .isConfident(false)
                    .build();
        }
    }

    private AIAnalysisResult parseAIDecision(String aiResponse, double confidenceThreshold) {
        try {
            // Extract JSON from the response (in case there's extra text)
            String jsonStr = aiResponse.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }

            log.info("[AI] Parsing AI decision JSON: {}", jsonStr);
            JsonNode decision = objectMapper.readTree(jsonStr);
            
            String statusStr = decision.get("status").asText();
            double confidence = decision.get("confidence").asDouble();
            String reasoning = decision.get("reasoning").asText();

            ReviewStatus status = "APPROVED".equals(statusStr) ? ReviewStatus.APPROVED : ReviewStatus.REJECTED;
            boolean isConfident = confidence >= confidenceThreshold;

            // If not confident, send to support agents
            if (!isConfident) {
                status = ReviewStatus.TO_SUPPORT_AGENTS;
            }

            log.info("[AI] Final moderation decision: status={}, confidence={}, reasoning={}, isConfident={}",
                    status, confidence, reasoning, isConfident);

            return AIAnalysisResult.builder()
                    .recommendedStatus(status)
                    .confidenceScore(confidence)
                    .analysis(reasoning)
                    .isConfident(isConfident)
                    .build();

        } catch (Exception e) {
            log.error("[AI] Error parsing AI decision: {}", e.getMessage(), e);
            return AIAnalysisResult.builder()
                    .recommendedStatus(ReviewStatus.TO_SUPPORT_AGENTS)
                    .confidenceScore(0.0)
                    .analysis("Error parsing AI decision: " + e.getMessage())
                    .isConfident(false)
                    .build();
        }
    }
}
