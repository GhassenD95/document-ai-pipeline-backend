package tn.finix.documentaipipelinebackend.service;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.finix.documentaipipelinebackend.config.RabbitConfig;
import tn.finix.documentaipipelinebackend.dto.DocumentMessage;
import tn.finix.documentaipipelinebackend.dto.DocumentResponse;
import tn.finix.documentaipipelinebackend.dto.UploadResponse;
import tn.finix.documentaipipelinebackend.exceptions.DocumentNotFoundException;
import tn.finix.documentaipipelinebackend.exceptions.InvalidFieldException;
import tn.finix.documentaipipelinebackend.model.Document;
import tn.finix.documentaipipelinebackend.repository.DocumentRepository;
import tn.finix.documentaipipelinebackend.util.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;

    public UploadResponse uploadDocument(MultipartFile file) {
        validateFile(file);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());

        try {
            document.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new InvalidFieldException("Failed to read file content");
        }

        Document saved = documentRepository.save(document);

        //Send Message to RabbitMQ
        DocumentMessage message = new DocumentMessage(
                saved.getId(),
                saved.getFileName(),
                saved.getContentType()
        );

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, message);

        return new UploadResponse(
                saved.getId(),
                saved.getFileName(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }


    private void validateFile(MultipartFile file) {
        if(file == null){
            throw new InvalidFieldException("File is null");
        }

        if(!FileUtils.isSizeLessThan50MB(file)){
            throw new InvalidFieldException("File size must be less than 50MB");
        }

        if(!FileUtils.isPdf(file)){
            throw new InvalidFieldException("File must be a PDF");
        }
    }


    public DocumentResponse getDocumentById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return toResponse(document);
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private DocumentResponse toResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getFileSize(),
                doc.getContentType(),
                doc.getStatus(),
                doc.getExtractedText(),
                doc.getExtractedData(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

}
