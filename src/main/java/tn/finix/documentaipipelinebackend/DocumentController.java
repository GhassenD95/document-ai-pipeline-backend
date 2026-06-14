package tn.finix.documentaipipelinebackend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.finix.documentaipipelinebackend.dto.DocumentResponse;

@RestController
public class DocumentController {

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> createDocument(@RequestBody MultipartFile file) {
        return
    }
}
