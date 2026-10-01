package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.repository.LeaseRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitOversizedDocumentTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestRepository leaseRequestRepository;

    @Test
    @DisplayName("Journey: Attempt submission with oversized document (>5MB) and assert file-size validation error")
    void testSubmitOversizedDocument() {
        String uniqueRequester = "OversizedUser_" + UUID.randomUUID().toString().substring(0, 8);
        registerCreatedRequesterId(uniqueRequester);

        File oversizedFile = getFixtureFile("oversized-file.pdf");
        assertTrue(oversizedFile.length() > 5 * 1024 * 1024, "Test fixture oversized-file.pdf must be > 5MB");

        driver.get(getBaseUrl() + "/submit-request");

        WebElement requesterInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("requesterId")));
        requesterInput.sendKeys(uniqueRequester);

        Select leaseTypeSelect = new Select(driver.findElement(By.id("leaseType")));
        leaseTypeSelect.selectByValue("Commercial");

        WebElement fileInput = driver.findElement(By.id("fileInput"));
        fileInput.sendKeys(oversizedFile.getAbsolutePath());

        WebElement submitBtn = driver.findElement(By.id("submit-btn"));
        submitBtn.click();

        // Assert inline error alert is displayed
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-alert")));
        assertTrue(errorAlert.isDisplayed(), "Error alert should be displayed for oversized file");

        WebElement errorMessage = driver.findElement(By.id("error-message"));
        String errorText = errorMessage.getText().toLowerCase();
        assertTrue(errorText.contains("5mb") || errorText.contains("5 mb") || errorText.contains("valid document") || errorText.contains("exceed"),
                "Error message should indicate file size violation (5 MB limit)");

        // Assert confirmation section is NOT displayed
        List<WebElement> confirmations = driver.findElements(By.id("confirmation-section"));
        assertTrue(confirmations.isEmpty(), "No confirmation card should be rendered when oversized file upload is rejected");

        // Verify through repository that no request was persisted for this unique requester
        boolean existsInDb = leaseRequestRepository.findAll().stream()
                .anyMatch(r -> uniqueRequester.equals(r.getRequesterId()));
        assertFalse(existsInDb, "Lease request with oversized document should not be persisted in the database");
    }
}
