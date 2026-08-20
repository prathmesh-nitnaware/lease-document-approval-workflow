# Lease Document Approval Workflow

A Spring Boot application for managing and approving lease documents through a structured workflow.

## Problem Statement

This project addresses the lease document approval problem identified during Week 1.

See the Week 1 project documentation for the complete problem statement.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Maven
- Spring Web
- Spring Data JPA
- MySQL
- Thymeleaf
- JUnit
- Selenium (planned for Week 9)

## Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL
- Git

### Clone the Repository

git clone <repository-url>
cd lease-document-approval-workflow

### Run the Application
mvn spring-boot:run

The application runs locally on:

http://localhost:8081

### Run Tests
mvn clean test

## Project Structure

src/
├── main/
│   ├── java/
│   │   └── com/example/leaseworkflow/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── model/
│   │       ├── repository/
│   │       └── service/
│   │
│   └── resources/
│       ├── templates/
│       └── application.properties/
│
└── test/
    └── java/

## Branching Convention

feature/<short-name> — new functionality
bugfix/<short-name> — bug fixes
release/<version> — release preparation

Main branches:

main — stable code
develop — integration/development branch