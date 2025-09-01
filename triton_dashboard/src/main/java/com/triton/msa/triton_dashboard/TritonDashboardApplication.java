package com.triton.msa.triton_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TritonDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(TritonDashboardApplication.class, args);
	}

}
