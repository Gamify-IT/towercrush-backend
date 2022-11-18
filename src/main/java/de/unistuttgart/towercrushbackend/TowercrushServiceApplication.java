package de.unistuttgart.towercrushbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TowercrushServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TowercrushServiceApplication.class, args);
    }
}
