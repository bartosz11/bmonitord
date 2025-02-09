package one.bartosz.bmonitord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "one.bartosz")
public class BmonitordApplication {

    //For the monolith purposes
    public static void main(String[] args) {
        SpringApplication.run(BmonitordApplication.class, args);
    }

}
