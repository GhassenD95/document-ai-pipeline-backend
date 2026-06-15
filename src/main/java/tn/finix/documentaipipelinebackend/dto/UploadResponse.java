package tn.finix.documentaipipelinebackend.dto;

import tn.finix.documentaipipelinebackend.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadResponse(
        UUID id,
        String fileName,
        DocumentStatus status,
        Instant createdAt
) {
}
