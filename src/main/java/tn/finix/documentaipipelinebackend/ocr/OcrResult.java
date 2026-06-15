package tn.finix.documentaipipelinebackend.ocr;

public record OcrResult(
        String text,
        double confidence,
        int pages
) {
}
