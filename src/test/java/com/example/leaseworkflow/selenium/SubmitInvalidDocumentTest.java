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

public class SubmitInvalidDocumentTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestRepository leaseRequestRepository;

    @Test
    @DisplayName("Journey 2: Attempt submission with invalid document type and assert inline validation error")
    void testSubmitInvalidDocument() {
        String uniqueRequester = "InvalidUser_" + UUID.randomUUID().toString().substring(0, 8);
        registerCreatedRequesterId(uniqueRequester);
        File invalidFile = getFixtureFile("invalid-type.txt");

        driver.get(getBaseUrl() + "/submit-request");

        WebElement requesterInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("requesterId")));
        requesterInput.sendKeys(uniqueRequester);

        Select leaseTypeSelect = new Select(driver.findElement(By.id("leaseType")));
        leaseTypeSelect.selectByValue("Residential");

        WebElement fileInput = driver.findElement(By.id("fileInput"));
        fileInput.sendKeys(invalidFile.getAbsolutePath());

        WebElement submitBtn = driver.findElement(By.id("submit-btn"));
        submitBtn.click();

        // Assert inline error alert is displayed
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-alert")));
        assertTrue(errorAlert.isDisplayed(), "Error alert should be displayed for invalid file type");

        WebElement errorMessage = driver.findElement(By.id("error-message"));
        assertTrue(errorMessage.getText().toLowerCase().contains("required") || errorMessage.getText().toLowerCase().contains("valid"),
                "Error message should mention document validation failure");

        // Assert confirmation card does NOT appear
        List<WebElement> confirmations = driver.findElements(By.id("confirmation-section"));
        assertTrue(confirmations.isEmpty(), "No confirmation card should be rendered on failure");

        // Assert request was NOT created in DB for this requester
        boolean existsInDb = leaseRequestRepository.findAll().stream()
                .anyMatch(r -> uniqueRequester.equals(r.getRequesterId()));
        assertFalse(existsInDb, "Lease request with invalid document should not be saved in the database");
    }
}
