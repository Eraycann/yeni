package org.kafka.evrak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EvrakApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvrakApplication.class, args);
    }

}
