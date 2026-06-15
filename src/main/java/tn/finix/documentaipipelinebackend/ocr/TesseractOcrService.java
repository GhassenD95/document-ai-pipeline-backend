package tn.finix.documentaipipelinebackend.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class TesseractOcrService implements OcrService {

    private final String tessdataPath;

    public TesseractOcrService(@Value("${docai.ocr.tessdata}") String tessdataPath) {
        this.tessdataPath = tessdataPath;
    }

    @Override
    public OcrResult extract(byte[] content, String fileName) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ocr_", "_" + sanitize(fileName));
            Files.write(tempFile, content);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng");

            String text = tesseract.doOCR(tempFile.toFile());

            return new OcrResult(text, 0.0, 1);
        } catch (TesseractException | IOException e) {
            log.error("OCR failed for file: {}", fileName, e);
            throw new RuntimeException("OCR processing failed", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }

    private String sanitize(String fileName) {
        return fileName != null ? fileName.replaceAll("[^a-zA-Z0-9._-]", "_") : "unknown";
    }
}
