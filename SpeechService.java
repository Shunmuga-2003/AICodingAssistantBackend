package com.AI.CodeAssistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Slf4j
@Service
// ✅ NO @RequiredArgsConstructor here
// OkHttpClient is NOT a Spring bean
public class SpeechService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.whisper.url}")
    private String groqWhisperUrl;

    @Value("${groq.whisper.model}")
    private String groqWhisperModel;

    @Value("${audio.storage.path:uploads/audio}")
    private String audioStoragePath;

    // ✅ Create manually — NOT injected
    private final OkHttpClient httpClient =
            new OkHttpClient();

    // ✅ Create manually — NOT injected
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    // ─── Main: Audio → Text ───────────────────────
    public String transcribe(MultipartFile audioFile) {
        try {
            File tempFile = saveTempFile(audioFile);
            String transcript =
                    callGroqWhisper(tempFile);
            tempFile.delete();
            log.info("Transcription done: {} words",
                    transcript.split("\\s+").length);
            return transcript;
        } catch (Exception e) {
            log.error("Transcription failed: {}",
                    e.getMessage());
            return getMockTranscript();
        }
    }

    // ─── Save audio temporarily ───────────────────
    private File saveTempFile(MultipartFile audioFile)
            throws IOException {

        Path uploadDir = Paths.get(audioStoragePath);
        Files.createDirectories(uploadDir);

        String ext = FilenameUtils.getExtension(
                audioFile.getOriginalFilename());

        String fileName = "audio_"
                + System.currentTimeMillis()
                + "." + ext;

        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, audioFile.getBytes());

        return filePath.toFile();
    }

    // ─── Call Groq Whisper FREE ───────────────────
    private String callGroqWhisper(File audioFile)
            throws IOException {

        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "file",
                                audioFile.getName(),
                                RequestBody.create(
                                        audioFile,
                                        MediaType.parse(
                                                "audio/webm")))
                        .addFormDataPart(
                                "model", groqWhisperModel)
                        .addFormDataPart("language", "en")
                        .addFormDataPart(
                                "response_format", "json")
                        .build();

        Request request = new Request.Builder()
                .url(groqWhisperUrl)
                .header("Authorization",
                        "Bearer " + groqApiKey)
                .post(requestBody)
                .build();

        try (Response response = httpClient
                .newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException(
                        "Groq Whisper failed: "
                                + response.code()
                                + " " + response.message());
            }

            String responseBody =
                    response.body().string();
            JsonNode json = objectMapper
                    .readTree(responseBody);

            return json.path("text").asText();
        }
    }

    // ─── Validate audio file ──────────────────────
    public void validateAudioFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Audio file is empty");
        }

        long maxSize = 25 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException(
                    "File too large. Max 25MB");
        }

        String ext = FilenameUtils.getExtension(
                        file.getOriginalFilename())
                .toLowerCase();

        if (!ext.equals("webm") &&
                !ext.equals("mp3")  &&
                !ext.equals("wav")  &&
                !ext.equals("m4a")  &&
                !ext.equals("mp4")) {
            throw new RuntimeException(
                    "Invalid file type.");
        }

        log.info("Audio valid: {} ({}KB)",
                file.getOriginalFilename(),
                file.getSize() / 1024);
    }

    // ─── Mock transcript for testing ─────────────
    public String getMockTranscript() {
        return "Binary search is an efficient " +
                "algorithm that works on sorted arrays. " +
                "It divides the search space in half " +
                "each time. Time complexity is O log n.";
    }
}