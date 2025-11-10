package io.github.jaredmcc4.gtm.validator;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidator {

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/xml",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "application/zip",
            "application/json",
            "application/sql",

            "image/svg+xml",
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp",
            "image/gif",

            "text/plain",
            "text/csv",
            "text/xml",
            "text/html",
            "text/css",
            "text/javascript",
            "text/x-java-source",
            "text/x-python",
            "text/x-c",
            "text/x-c++src",
            "text/x-csharp"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    public static void validate(MultipartFile file) {
        if (file.isEmpty()){
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (file.getSize() > MAX_SIZE){
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 10MB.");
        }
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())){
            throw new IllegalArgumentException("El tipo de archivo no está permitido.");
        }
    }
}
