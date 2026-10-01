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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewerApprovesRequestTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Test
    @DisplayName("Journey 3: Reviewer locates a seeded request and approves it")
    void testReviewerApprovesRequest() {
        String uniqueRequester = "ApproverTest_" + UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "contract.pdf",
                "application/pdf",
                "Approved Contract Sample Content".getBytes(StandardCharsets.UTF_8)
        );

        LeaseRequestResponseDto seeded = leaseRequestService.submitRequest(uniqueRequester, "Commercial", List.of(file));
        Long requestId = seeded.getId();
        registerCreatedRequest(requestId);
        registerCreatedRequesterId(uniqueRequester);

        driver.get(getBaseUrl() + "/reviewer-queue");

        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("request-row-" + requestId)));
        assertTrue(row.isDisplayed(), "Seeded request row must be visible in reviewer queue");

        WebElement approveBtn = driver.findElement(By.id("approve-btn-" + requestId));
        approveBtn.click();

        // Assert success alert
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-alert")));
        assertTrue(successAlert.getText().contains("APPROVED"), "Success alert should confirm request approval");

        // Assert status is now APPROVED in service/repository
        Optional<LeaseRequestResponseDto> updatedReq = leaseRequestService.getRequestById(requestId);
        assertTrue(updatedReq.isPresent());
        assertEquals("APPROVED", updatedReq.get().getStatus());

        // Assert reviewer queue no longer lists it
        List<WebElement> remainingRow = driver.findElements(By.id("request-row-" + requestId));
        assertTrue(remainingRow.isEmpty(), "Approved request should no longer be listed in the reviewer queue");
    }
}
