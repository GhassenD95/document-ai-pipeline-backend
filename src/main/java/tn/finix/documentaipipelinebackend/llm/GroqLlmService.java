package tn.finix.documentaipipelinebackend.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GroqLlmService implements LlmService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GroqLlmService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = dotenv.get("GROK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROK_API_KEY not found in .env file");
        }
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String extractFields(String text, String documentType) {
        String prompt = """
                Extract the following fields from this %s and return them as valid JSON:
                - total (string)
                - date (string)
                - vendor (string)
                - invoiceNumber (string)
                If a field is not found, use null.
                Return only the JSON object, no explanation.
                """.formatted(documentType);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "llama-3.1-8b-instant");
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 500);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message("system", prompt));
        messages.add(message("user", text));
        requestBody.set("messages", messages);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String response = restClient.post()
                .uri("/v1/chat/completions")
                .body(jsonBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            String json = root.get("choices").get(0).get("message").get("content").asText();
            log.info("LLM extraction result for {}: {}", documentType, json);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    private JsonNode message(String role, String content) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }
}
