package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SubmitValidLeaseRequestTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Test
    @DisplayName("Journey 1: Submit valid lease request with PDF attachment")
    void testSubmitValidLeaseRequest() {
        String uniqueRequester = "Requester_" + UUID.randomUUID().toString().substring(0, 8);
        registerCreatedRequesterId(uniqueRequester);
        File pdfFile = getFixtureFile("sample-lease-doc.pdf");

        driver.get(getBaseUrl() + "/submit-request");

        WebElement requesterInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("requesterId")));
        requesterInput.sendKeys(uniqueRequester);

        Select leaseTypeSelect = new Select(driver.findElement(By.id("leaseType")));
        leaseTypeSelect.selectByValue("Commercial");

        WebElement fileInput = driver.findElement(By.id("fileInput"));
        fileInput.sendKeys(pdfFile.getAbsolutePath());

        WebElement submitBtn = driver.findElement(By.id("submit-btn"));
        submitBtn.click();

        WebElement confirmationSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirmation-section")));
        assertTrue(confirmationSection.isDisplayed(), "Confirmation card must be displayed upon successful submission");

        WebElement requestIdElem = driver.findElement(By.id("confirmation-request-id"));
        String reqIdText = requestIdElem.getText().replace("#", "").trim();
        assertFalse(reqIdText.isEmpty(), "Request ID must not be empty");
        Long createdId = Long.parseLong(reqIdText);

        WebElement statusElem = driver.findElement(By.id("confirmation-status"));
        assertEquals("SUBMITTED", statusElem.getText().trim());

        // Verify state via tracking view
        driver.get(getBaseUrl() + "/track-request/" + createdId);
        WebElement trackingStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-badge")));
        assertEquals("SUBMITTED", trackingStatus.getText().trim());
        WebElement trackingRequester = driver.findElement(By.id("track-requester-id"));
        assertEquals(uniqueRequester, trackingRequester.getText().trim());

        // Also verify in repository/service layer
        Optional<LeaseRequestResponseDto> requestOpt = leaseRequestService.getRequestById(createdId);
        assertTrue(requestOpt.isPresent());
        assertEquals("SUBMITTED", requestOpt.get().getStatus());
        assertEquals(uniqueRequester, requestOpt.get().getRequesterId());
    }
}
