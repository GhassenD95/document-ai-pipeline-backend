package tn.finix.documentaipipelinebackend.llm;

public interface LlmService {
    String extractFields(String text, String documentType);
}
