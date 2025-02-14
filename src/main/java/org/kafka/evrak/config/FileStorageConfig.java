package org.kafka.evrak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    private String uploadsDir;

    public String getUploadsDir() {
        return uploadsDir;
    }

    public void setUploadsDir(String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    public Path getUploadsPath() {
        return Paths.get(uploadsDir).toAbsolutePath().normalize();
    }
}
