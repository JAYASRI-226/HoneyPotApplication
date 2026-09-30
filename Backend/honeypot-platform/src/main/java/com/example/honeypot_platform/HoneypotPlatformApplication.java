package com.example.honeypot_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HoneypotPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoneypotPlatformApplication.class, args);
	}

}
