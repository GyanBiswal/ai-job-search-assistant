package com.aijobsearch.backend.service;

import com.aijobsearch.backend.dto.ResumeResponse;
import com.aijobsearch.backend.entity.Resume;
import com.aijobsearch.backend.entity.User;
import com.aijobsearch.backend.exception.InvalidFileException;
import com.aijobsearch.backend.exception.ResumeNotFoundException;
import com.aijobsearch.backend.exception.ResumeProcessingException;
import com.aijobsearch.backend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public ResumeResponse uploadResume(MultipartFile file, User user) {
        validateFile(file);

        String extractedText = extractText(file);

        Optional<Resume> existing = resumeRepository.findByUserId(user.getId());
        existing.ifPresent(r -> deleteFileQuietly(r.getStoredFilePath()));

        String storedFilePath = storeFile(file, user.getId());

        Resume resume = existing.orElse(Resume.builder().user(user).build());
        resume.setOriginalFileName(file.getOriginalFilename());
        resume.setStoredFilePath(storedFilePath);
        resume.setExtractedText(extractedText);
        resume.setUploadedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);
        return toResponse(saved);
    }

    public ResumeResponse getMyResume(Long userId) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResumeNotFoundException("No resume uploaded yet"));
        return toResponse(resume);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new InvalidFileException("Only PDF files are supported");
        }
    }

    private String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new InvalidFileException(
                        "Could not extract any text from this PDF. It may be a scanned image without a text layer."
                );
            }
            return text;
        } catch (IOException e) {
            throw new ResumeProcessingException("Failed to read PDF file: " + e.getMessage());
        }
    }

    private String storeFile(MultipartFile file, Long userId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = userId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            throw new ResumeProcessingException("Failed to store file: " + e.getMessage());
        }
    }

    private void deleteFileQuietly(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException ignored) {
            // Old file cleanup failing shouldn't block a new upload
        }
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .extractedText(resume.getExtractedText())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }
}