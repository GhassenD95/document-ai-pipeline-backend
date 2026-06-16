package tn.finix.documentaipipelinebackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.finix.documentaipipelinebackend.dto.DocumentResponse;
import tn.finix.documentaipipelinebackend.dto.UploadResponse;
import tn.finix.documentaipipelinebackend.service.DocumentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@AllArgsConstructor
@Tag(name = "Documents", description = "Document upload and management API")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a PDF document for processing")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = documentService.uploadDocument(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents by text")
    public ResponseEntity<List<DocumentResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(documentService.searchDocuments(query));
    }

    @PostMapping("/generate-sample")
    @Operation(summary = "Generate a sample invoice PDF for testing")
    public ResponseEntity<UploadResponse> generateSample() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentService.generateSamplePdf());
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download the original PDF file")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        byte[] data = documentService.getDocumentFileData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"document.pdf\"")
                .body(data);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry processing a failed document")
    public ResponseEntity<Void> retry(@PathVariable UUID id) {
        documentService.retryDocument(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping
    @Operation(summary = "List all documents")
    public ResponseEntity<List<DocumentResponse>> listDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }
}
