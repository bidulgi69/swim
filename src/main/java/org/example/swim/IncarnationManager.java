package org.example.swim;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Component
public class IncarnationManager {

    @Value("${swim.id:}")
    private String id;

    private Path path() {
        return Path.of("/var/log/swim/incarnation-" + id + ".txt");
    }

    public long loadAndBump(String id) {
        if (!this.id.equals(id)) {
            return 0L;
        }

        // stale resurrection
        long previous = readFromDisk();
        long now = Instant.now().getEpochSecond();
        long bumped = Math.max(previous + 1, now);
        writeToDisk(bumped);
        return bumped;
    }

    private long readFromDisk() {
        Path path = path();
        if (!Files.exists(path)) return 0L;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return Long.parseLong(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            return 0L;
        }
    }

    private void writeToDisk(long value) {
        Path filePath = path();
        try {
            if (Files.notExists(filePath)) {
                // 파일이 존재하지 않으면 생성
                Files.createDirectories(filePath.getParent()); // 필요한 부모 디렉토리까지 생성
                Files.createFile(filePath);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write(Long.toString(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
