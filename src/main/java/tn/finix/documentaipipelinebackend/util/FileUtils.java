package tn.finix.documentaipipelinebackend.util;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    //50MB
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private FileUtils() {
    }

    public static boolean isPdf(MultipartFile file) {

        return "application/pdf".equals(file.getContentType());
    }

    public static boolean isSizeLessThan50MB(MultipartFile file) {
        return file.getSize() < MAX_SIZE;
    }
}
