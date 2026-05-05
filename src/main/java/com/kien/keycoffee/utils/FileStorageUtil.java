package com.kien.keycoffee.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class FileStorageUtil {

    private static final String DEFAULT_FILE_NAME = "image";
    private static final String INVALID_FILE_PATH_MESSAGE = "Invalid file path";

    private FileStorageUtil() {
    }

    public static String saveFile(MultipartFile file, String uploadDir, String relativeDir) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalName = Path.of(file.getOriginalFilename() == null ? DEFAULT_FILE_NAME : file.getOriginalFilename())
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        String fileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadPath.resolve(fileName).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException(INVALID_FILE_PATH_MESSAGE);
        }

        file.transferTo(targetPath);

        return normalizeRelativeDir(relativeDir) + "/" + fileName;
    }


    public static void deletePublicFile(String publicPath, String staticRootDir) {
        try {
            if (publicPath == null || publicPath.isBlank()) {
                return;
            }

            String normalizedPublicPath = publicPath.replace("\\", "/");
            while (normalizedPublicPath.startsWith("/")) {
                normalizedPublicPath = normalizedPublicPath.substring(1);
            }

            Path staticRoot = Paths.get(staticRootDir).toAbsolutePath().normalize();
            Path targetPath = staticRoot.resolve(normalizedPublicPath).normalize();
            if (!targetPath.startsWith(staticRoot)) {
                return;
            }

            Files.deleteIfExists(targetPath);
        } catch (Exception ignored) {
        }
    }

    private static String normalizeRelativeDir(String relativeDir) {
        String normalized = relativeDir == null ? "" : relativeDir.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
