package tn.finix.documentaipipelinebackend.dto;

import tn.finix.documentaipipelinebackend.model.DocumentStatus;


public record DocumentRequest(
        String fileName,
        long fileSize,
        String contentType,
        DocumentStatus status

) {
}
