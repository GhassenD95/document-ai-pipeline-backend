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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
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

    public List<DocumentResponse> searchDocuments(String query) {
        if (query == null || query.isBlank()) {
            return getAllDocuments();
        }
        return documentRepository.searchByText(query).stream()
                .map(this::toResponse)
                .toList();
    }

    public UploadResponse generateSamplePdf() {
        Random rand = new Random();
        String[] vendors = {"Acme Corp", "TechStore Inc.", "Global Services LLC", "DataPro Solutions", "CloudBase Ltd"};
        String vendor = vendors[rand.nextInt(vendors.length)];
        double total = 50 + rand.nextDouble() * 2000;
        LocalDate date = LocalDate.now().minusDays(rand.nextInt(60));
        String invoiceNum = "INV-" + (1000 + rand.nextInt(9000));
        int qty = 1 + rand.nextInt(10);
        double unitPrice = total / qty;

        String fileName = "invoice_" + invoiceNum + ".pdf";

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                cs.setFont(bold, 24);
                cs.beginText();
                cs.newLineAtOffset(50, 750);
                cs.showText("INVOICE");
                cs.endText();

                cs.setFont(regular, 11);
                cs.beginText();
                cs.newLineAtOffset(50, 720);
                cs.showText("Invoice #: " + invoiceNum);
                cs.newLineAtOffset(0, -18);
                cs.showText("Date: " + date);
                cs.newLineAtOffset(0, -18);
                cs.showText("Vendor: " + vendor);
                cs.endText();

                cs.setFont(bold, 14);
                cs.beginText();
                cs.newLineAtOffset(50, 650);
                cs.showText("Bill To:");
                cs.endText();

                cs.setFont(regular, 11);
                cs.beginText();
                cs.newLineAtOffset(50, 630);
                cs.showText("Client: Document AI Pipeline Demo");
                cs.newLineAtOffset(0, -18);
                cs.showText("Address: 123 Demo Street, Tunis");
                cs.endText();

                float tableY = 580;
                cs.setFont(bold, 11);
                cs.setLineWidth(1);
                cs.moveTo(50, tableY);
                cs.lineTo(545, tableY);
                cs.stroke();

                cs.beginText();
                cs.newLineAtOffset(55, tableY - 16);
                cs.showText("Description");
                cs.newLineAtOffset(250, 0);
                cs.showText("Qty");
                cs.newLineAtOffset(60, 0);
                cs.showText("Unit Price");
                cs.newLineAtOffset(80, 0);
                cs.showText("Total");
                cs.endText();

                cs.moveTo(50, tableY - 22);
                cs.lineTo(545, tableY - 22);
                cs.stroke();

                cs.setFont(regular, 11);
                cs.beginText();
                cs.newLineAtOffset(55, tableY - 40);
                cs.showText(vendor + " - Professional Services");
                cs.newLineAtOffset(250, 0);
                cs.showText(String.valueOf(qty));
                cs.newLineAtOffset(60, 0);
                cs.showText(String.format("$%.2f", unitPrice));
                cs.newLineAtOffset(80, 0);
                cs.showText(String.format("$%.2f", total));
                cs.endText();

                cs.moveTo(50, tableY - 48);
                cs.lineTo(545, tableY - 48);
                cs.stroke();

                cs.setFont(bold, 14);
                cs.beginText();
                cs.newLineAtOffset(380, tableY - 80);
                cs.showText(String.format("Total: $%.2f", total));
                cs.endText();

                cs.setFont(regular, 9);
                cs.beginText();
                cs.newLineAtOffset(50, 100);
                cs.showText("Thank you for your business. Payment due within 30 days.");
                cs.endText();
            }

            byte[] pdfBytes;
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                doc.save(baos);
                pdfBytes = baos.toByteArray();
            }

            Document document = new Document();
            document.setFileName(fileName);
            document.setContentType("application/pdf");
            document.setFileSize(pdfBytes.length);
            document.setFileData(pdfBytes);

            Document saved = documentRepository.save(document);

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
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate sample PDF", e);
        }
    }

    public byte[] getDocumentFileData(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
        return document.getFileData();
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
