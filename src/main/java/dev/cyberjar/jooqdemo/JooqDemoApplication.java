package dev.cyberjar.jooqdemo;

import dev.cyberjar.jooqdemo.service.AppointmentSlotService;
import dev.cyberjar.jooqdemo.service.TriageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JooqDemoApplication implements CommandLineRunner {

    private final AppointmentSlotService slotService;
    private final TriageService triageService;


    public JooqDemoApplication(AppointmentSlotService slotService, TriageService triageService) {
        this.slotService = slotService;
        this.triageService = triageService;
    }

    public static void main(String[] args) {
        SpringApplication.run(JooqDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


    }
}
