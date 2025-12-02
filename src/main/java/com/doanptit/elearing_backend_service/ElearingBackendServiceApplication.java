package com.doanptit.elearing_backend_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElearingBackendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElearingBackendServiceApplication.class, args);
	}

}
