package com.example.leaseworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.leaseworkflow.repository")
public class LeaseDocumentApprovalWorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaseDocumentApprovalWorkflowApplication.class, args);
	}

}
