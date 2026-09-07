package com.example.leaseworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.leaseworkflow.repository")
@EntityScan(basePackages = "com.example.leaseworkflow.model")
public class LeaseDocumentApprovalWorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaseDocumentApprovalWorkflowApplication.class, args);
	}

}
