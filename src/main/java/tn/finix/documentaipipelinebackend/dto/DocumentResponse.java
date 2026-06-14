package tn.finix.documentaipipelinebackend.dto;

import tn.finix.documentaipipelinebackend.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        long fileSize,
        String contentType,
        DocumentStatus status,
        String extractedText,
        String extractedData,
        Instant createdAt,
        Instant updatedAt
) {
}
