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

public class ChangesRequestedThenResubmitTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Test
    @DisplayName("Journey 5: Reviewer requests changes with comment, requester views tracking view and resubmits")
    void testChangesRequestedThenResubmit() {
        String uniqueRequester = "ResubmitTest_" + UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "initial-draft.pdf",
                "application/pdf",
                "Initial Draft Content".getBytes(StandardCharsets.UTF_8)
        );

        LeaseRequestResponseDto seeded = leaseRequestService.submitRequest(uniqueRequester, "Industrial", List.of(file));
        Long requestId = seeded.getId();
        registerCreatedRequest(requestId);
        registerCreatedRequesterId(uniqueRequester);

        // 1. Reviewer opens reviewer queue and clicks 'Request Changes'
        driver.get(getBaseUrl() + "/reviewer-queue");

        WebElement changesBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("changes-btn-" + requestId)));
        changesBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("commentModal")));
        assertTrue(modal.isDisplayed());

        WebElement commentInput = driver.findElement(By.id("modalComment"));
        String changeNote = "Please upload signed appendix C and higher quality tax certificate";
        commentInput.sendKeys(changeNote);

        WebElement submitBtn = driver.findElement(By.id("modalSubmitBtn"));
        submitBtn.click();

        // 2. Assert status transitioned to CHANGES_REQUESTED in DB
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-alert")));
        Optional<LeaseRequestResponseDto> changeReq = leaseRequestService.getRequestById(requestId);
        assertTrue(changeReq.isPresent());
        assertEquals("CHANGES_REQUESTED", changeReq.get().getStatus());

        // 3. Requester navigates to track-request view
        driver.get(getBaseUrl() + "/track-request/" + requestId);

        WebElement statusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-badge")));
        assertEquals("CHANGES_REQUESTED", statusBadge.getText().trim());

        WebElement resubmitSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resubmit-section")));
        assertTrue(resubmitSection.isDisplayed(), "Resubmission section must be visible when status is CHANGES_REQUESTED");

        // 4. Attach revised document and submit
        File revisedDoc = getFixtureFile("sample-lease-doc.pdf");
        WebElement resubmitFileInput = driver.findElement(By.id("resubmitFileInput"));
        resubmitFileInput.sendKeys(revisedDoc.getAbsolutePath());

        WebElement resubmitBtn = driver.findElement(By.id("resubmit-btn"));
        resubmitBtn.click();

        // 5. Assert status returns to SUBMITTED
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-alert")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("status-badge"), "SUBMITTED"));
        WebElement newStatusBadge = driver.findElement(By.id("status-badge"));
        assertEquals("SUBMITTED", newStatusBadge.getText().trim());

        Optional<LeaseRequestResponseDto> resubmittedReq = leaseRequestService.getRequestById(requestId);
        assertTrue(resubmittedReq.isPresent());
        assertEquals("SUBMITTED", resubmittedReq.get().getStatus());
    }
}
