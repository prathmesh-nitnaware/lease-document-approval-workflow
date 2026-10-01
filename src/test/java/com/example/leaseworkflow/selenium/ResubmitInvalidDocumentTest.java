package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ResubmitInvalidDocumentTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Test
    @DisplayName("Regression: Attempt resubmission with invalid document fails validation and keeps CHANGES_REQUESTED status")
    void testResubmitInvalidDocument() {
        String uniqueRequester = "InvalidResubmit_" + UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile initialFile = new MockMultipartFile(
                "files",
                "original-lease.pdf",
                "application/pdf",
                "Valid Initial Content".getBytes(StandardCharsets.UTF_8)
        );

        // 1. Seed request in SUBMITTED status
        LeaseRequestResponseDto seeded = leaseRequestService.submitRequest(uniqueRequester, "Commercial", List.of(initialFile));
        Long requestId = seeded.getId();
        registerCreatedRequest(requestId);
        registerCreatedRequesterId(uniqueRequester);

        // 2. Put request into CHANGES_REQUESTED status
        leaseRequestService.requestChanges(requestId, "Reviewer-01", "Please supply updated tax registration form");
        assertEquals("CHANGES_REQUESTED", leaseRequestService.getRequestById(requestId).get().getStatus());

        // 3. Navigate to tracking view
        driver.get(getBaseUrl() + "/track-request/" + requestId);

        WebElement initialBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-badge")));
        assertEquals("CHANGES_REQUESTED", initialBadge.getText().trim());

        WebElement resubmitSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resubmit-section")));
        assertTrue(resubmitSection.isDisplayed(), "Resubmission section must be visible for CHANGES_REQUESTED request");

        // 4. Upload an invalid document (invalid-type.txt)
        File invalidDoc = getFixtureFile("invalid-type.txt");
        WebElement resubmitFileInput = driver.findElement(By.id("resubmitFileInput"));
        resubmitFileInput.sendKeys(invalidDoc.getAbsolutePath());

        // 5. Submit resubmission
        WebElement resubmitBtn = driver.findElement(By.id("resubmit-btn"));
        resubmitBtn.click();

        // 6. Verify error alert is displayed
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-alert")));
        assertTrue(errorAlert.isDisplayed(), "Error alert should be displayed on invalid resubmission");

        WebElement errorMessage = driver.findElement(By.id("error-message"));
        String errorText = errorMessage.getText().toLowerCase();
        assertTrue(errorText.contains("required") || errorText.contains("valid") || errorText.contains("unsupported"),
                "Error message should explain document validation failure on resubmission");

        // 7. Verify status remains CHANGES_REQUESTED (did NOT transition to SUBMITTED)
        WebElement statusBadge = driver.findElement(By.id("status-badge"));
        assertEquals("CHANGES_REQUESTED", statusBadge.getText().trim(),
                "Status must remain CHANGES_REQUESTED when resubmission has invalid documents");

        Optional<LeaseRequestResponseDto> reqInDb = leaseRequestService.getRequestById(requestId);
        assertTrue(reqInDb.isPresent());
        assertEquals("CHANGES_REQUESTED", reqInDb.get().getStatus(),
                "Database status must remain CHANGES_REQUESTED and NOT become SUBMITTED");

        // 8. Verify resubmission card is still present to allow user re-attempt
        WebElement resubmitCardStillPresent = driver.findElement(By.id("resubmit-section"));
        assertTrue(resubmitCardStillPresent.isDisplayed(), "Resubmit form should stay visible after failed resubmission");
    }
}
