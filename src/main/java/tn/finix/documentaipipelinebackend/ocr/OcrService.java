package tn.finix.documentaipipelinebackend.ocr;

public interface OcrService {
    OcrResult extract(byte[] content, String fileName);
}
