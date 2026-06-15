package tn.finix.documentaipipelinebackend.dto;

import java.util.UUID;

public record DocumentMessage(
        UUID documentId,
        String fileName,
        String contentType
) {
}
