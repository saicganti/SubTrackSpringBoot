package com.example.subtrackapispringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SubTrackApiSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubTrackApiSpringbootApplication.class,
                "--spring.datasource.url=jdbc:postgresql://localhost:5432/subtrack",
                "--spring.datasource.username=postgres",
                "--spring.datasource.password=dev",
                "--spring.flyway.url=jdbc:postgresql://localhost:5432/subtrack",
                "--spring.flyway.user=postgres",
                "--spring.flyway.password=dev",
                "--spring.jpa.hibernate.ddl-auto=validate",
                "--spring.flyway.enabled=true"
        );
    }
}
