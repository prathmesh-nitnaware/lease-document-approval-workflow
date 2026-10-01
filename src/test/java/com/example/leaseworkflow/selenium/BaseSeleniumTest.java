package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.model.StatusHistory;
import com.example.leaseworkflow.repository.DocumentRepository;
import com.example.leaseworkflow.repository.LeaseRequestRepository;
import com.example.leaseworkflow.repository.ReviewActionRepository;
import com.example.leaseworkflow.repository.StatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected LeaseRequestRepository leaseRequestRepository;

    @Autowired
    protected DocumentRepository documentRepository;

    @Autowired
    protected ReviewActionRepository reviewActionRepository;

    @Autowired
    protected StatusHistoryRepository statusHistoryRepository;

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected final Set<Long> createdRequestIds = Collections.synchronizedSet(new HashSet<>());
    protected final Set<String> createdRequesterIds = Collections.synchronizedSet(new HashSet<>());

    @BeforeEach
    public void setUpDriver() {
        ChromeOptions options = new ChromeOptions();

        // System property -Dselenium.headless=false allows visible browser for debugging; defaults to true for CI/headless runs
        String headlessProp = System.getProperty("selenium.headless", "true");
        boolean isHeadless = Boolean.parseBoolean(headlessProp);

        if (isHeadless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void cleanupTestData() {
        try {
            // Find any requests matching registered requester IDs if ID wasn't explicitly registered
            for (String requesterId : new HashSet<>(createdRequesterIds)) {
                try {
                    List<LeaseRequest> requests = leaseRequestRepository.findByRequesterId(requesterId);
                    for (LeaseRequest req : requests) {
                        createdRequestIds.add(req.getId());
                    }
                } catch (Exception ignored) {
                }
            }

            // Remove test-created records in child-first order
            for (Long id : new HashSet<>(createdRequestIds)) {
                try {
                    List<Document> docs = documentRepository.findByRequestId(id);
                    if (!docs.isEmpty()) {
                        documentRepository.deleteAll(docs);
                    }
                    List<ReviewAction> actions = reviewActionRepository.findByRequestId(id);
                    if (!actions.isEmpty()) {
                        reviewActionRepository.deleteAll(actions);
                    }
                    List<StatusHistory> histories = statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(id);
                    if (!histories.isEmpty()) {
                        statusHistoryRepository.deleteAll(histories);
                    }
                    leaseRequestRepository.deleteById(id);
                } catch (Exception e) {
                    System.err.println("[BaseSeleniumTest] Error cleaning test data for request #" + id + ": " + e.getMessage());
                }
            }
        } finally {
            createdRequestIds.clear();
            createdRequesterIds.clear();
        }
    }

    protected void registerCreatedRequest(Long requestId) {
        if (requestId != null) {
            createdRequestIds.add(requestId);
        }
    }

    protected void registerCreatedRequesterId(String requesterId) {
        if (requesterId != null) {
            createdRequesterIds.add(requesterId);
        }
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public void quitDriver() {
        if (this.driver != null) {
            try {
                this.driver.quit();
            } catch (Exception ignored) {
            } finally {
                this.driver = null;
            }
        }
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    protected File getFixtureFile(String filename) {
        File file = new File("src/test/resources/test-data/" + filename);
        if (!file.exists()) {
            throw new IllegalArgumentException("Test fixture not found: " + file.getAbsolutePath());
        }
        return file.getAbsoluteFile();
    }
}
