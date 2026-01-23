package dev.cyberjar.jooqdemo;

import dev.cyberjar.jooqdemo.service.NeonCareService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JooqDemoApplication implements CommandLineRunner {

    private final NeonCareService neonCareService;

    public JooqDemoApplication(NeonCareService neonCareService) {
        this.neonCareService = neonCareService;
    }

    public static void main(String[] args) {
        SpringApplication.run(JooqDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
