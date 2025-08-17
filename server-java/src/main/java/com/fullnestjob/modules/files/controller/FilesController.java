package com.fullnestjob.modules.files.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/files")
public class FilesController {

    private final Path uploadRoot = Paths.get(System.getProperty("user.dir"))
            .resolve("uploads").toAbsolutePath().normalize();

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> upload(
            @RequestParam("fileUpload") MultipartFile file,
            @RequestHeader(value = "folder_type", required = false) String folderType
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > -1 && dot < originalName.length() - 1) {
            ext = originalName.substring(dot);
        }

        String newFileName = UUID.randomUUID().toString() + ext;
        Path baseDir = uploadRoot;
        if (folderType != null && !folderType.isBlank()) {
            baseDir = baseDir.resolve(folderType);
        }
        Files.createDirectories(baseDir);
        Path target = baseDir.resolve(newFileName);
        file.transferTo(target);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/files/")
                .path(folderType != null && !folderType.isBlank() ? folderType + "/" : "")
                .path(newFileName)
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("fileName", newFileName);
        body.put("originalName", originalName);
        body.put("size", file.getSize());
        body.put("contentType", file.getContentType());
        body.put("folder", folderType);
        body.put("url", url);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws IOException {
        Path file = uploadRoot.resolve(fileName).normalize();
        return buildFileResponse(file);
    }

    @GetMapping("/{folder}/{fileName}")
    public ResponseEntity<Resource> getFileInFolder(@PathVariable String folder, @PathVariable String fileName) throws IOException {
        Path file = uploadRoot.resolve(folder).resolve(fileName).normalize();
        return buildFileResponse(file);
    }

    private ResponseEntity<Resource> buildFileResponse(Path file) throws IOException {
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource;
        try {
            resource = new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
        String contentType = Files.probeContentType(file);
        if (contentType == null || contentType.equals(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            String name = file.getFileName().toString().toLowerCase();
            if (name.endsWith(".pdf")) contentType = MediaType.APPLICATION_PDF_VALUE;
            else if (name.endsWith(".doc")) contentType = "application/msword";
            else if (name.endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            else if (name.endsWith(".png")) contentType = MediaType.IMAGE_PNG_VALUE;
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = MediaType.IMAGE_JPEG_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName().toString() + "\"")
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}


