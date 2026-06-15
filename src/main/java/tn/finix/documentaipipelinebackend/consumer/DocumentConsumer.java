package tn.finix.documentaipipelinebackend.consumer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.finix.documentaipipelinebackend.dto.DocumentMessage;
import tn.finix.documentaipipelinebackend.model.Document;
import tn.finix.documentaipipelinebackend.model.DocumentStatus;
import tn.finix.documentaipipelinebackend.repository.DocumentRepository;

@Component
@AllArgsConstructor
@Slf4j
public class DocumentConsumer {

    private final DocumentRepository documentRepository;

    @RabbitListener(queues = "${docai.rabbitmq.queue:document.processing}")
    public void processDocument(DocumentMessage message) {
        log.info("Received document to process: {}", message.documentId());

        Document document = documentRepository.findById(message.documentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + message.documentId()));

        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            log.info("Processing document: {}", message.fileName());
            Thread.sleep(2000);

            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);

            log.info("Document processed successfully: {}", message.documentId());
        } catch (Exception e) {
            log.error("Failed to process document: {}", message.documentId(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
            throw new RuntimeException("Document processing failed: " + message.documentId(), e);
        }
    }
}
