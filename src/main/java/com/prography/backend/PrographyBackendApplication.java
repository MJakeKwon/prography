package com.prography.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PrographyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrographyBackendApplication.class, args);
	}

}
