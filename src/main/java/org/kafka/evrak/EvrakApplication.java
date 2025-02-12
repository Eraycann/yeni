package org.kafka.evrak;

import org.kafka.evrak.config.FileStorageConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableJpaAuditing
public class EvrakApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvrakApplication.class, args);
    }

    @Bean
    CommandLineRunner init(FileStorageConfig fileStorageConfig) {
        return args -> {
            Path uploadsPath = fileStorageConfig.getUploadsPath();
            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
            }
        };
    }
}
