package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.dto.LeaseRequestResponseDto;
import com.example.leaseworkflow.model.ReviewAction;
import com.example.leaseworkflow.service.LeaseRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewerRejectsWithCommentTest extends BaseSeleniumTest {

    @Autowired
    private LeaseRequestService leaseRequestService;

    @Test
    @DisplayName("Journey 4: Reviewer attempts reject without comment (blocked via UI & server-side), then rejects with mandatory comment")
    void testReviewerRejectsWithComment() {
        String uniqueRequester = "RejectTest_" + UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "rejected-lease.pdf",
                "application/pdf",
                "Reject Content".getBytes(StandardCharsets.UTF_8)
        );

        LeaseRequestResponseDto seeded = leaseRequestService.submitRequest(uniqueRequester, "Equipment", List.of(file));
        Long requestId = seeded.getId();
        registerCreatedRequest(requestId);
        registerCreatedRequesterId(uniqueRequester);

        // Step 4a-1: Server-side validation check — assert backend rejects empty comment
        IllegalArgumentException serverEx = assertThrows(
                IllegalArgumentException.class,
                () -> leaseRequestService.rejectRequest(requestId, "Reviewer-01", "   ")
        );
        assertTrue(serverEx.getMessage().contains("comment is mandatory to reject a request"),
                "Backend service should throw IllegalArgumentException for blank comment");

        // Verify request is still SUBMITTED and no REJECT review actions exist
        assertEquals("SUBMITTED", leaseRequestService.getRequestById(requestId).get().getStatus());
        List<ReviewAction> reviewActions = reviewActionRepository.findByRequestId(requestId);
        assertTrue(reviewActions.stream().noneMatch(a -> "REJECT".equalsIgnoreCase(a.getAction())),
                "No REJECT action should be persisted for empty comment attempt");

        // Step 4a-2: Client-side UI validation check via Selenium
        driver.get(getBaseUrl() + "/reviewer-queue");

        WebElement rejectBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("reject-btn-" + requestId)));
        rejectBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("commentModal")));
        assertTrue(modal.isDisplayed(), "Comment modal should be opened");

        WebElement commentInput = driver.findElement(By.id("modalComment"));
        WebElement submitBtn = driver.findElement(By.id("modalSubmitBtn"));

        // Assert mandatory comment enforcement when empty in browser
        Boolean isValid = (Boolean) ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", commentInput);
        assertFalse(isValid, "Empty comment textarea must fail validity check in browser");

        submitBtn.click();

        // Assert DB status remains SUBMITTED (action blocked)
        Optional<LeaseRequestResponseDto> reqStillSubmitted = leaseRequestService.getRequestById(requestId);
        assertTrue(reqStillSubmitted.isPresent());
        assertEquals("SUBMITTED", reqStillSubmitted.get().getStatus(), "Status should remain SUBMITTED when comment is omitted");

        // Step 4b: Provide mandatory comment and submit rejection via UI
        String rejectComment = "Missing counter-signature on section 4.B";
        commentInput.sendKeys(rejectComment);

        submitBtn.click();

        // Assert success message on queue
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-alert")));
        assertTrue(successAlert.getText().contains("REJECTED"));

        // Assert status in DB is REJECTED
        Optional<LeaseRequestResponseDto> rejectedReq = leaseRequestService.getRequestById(requestId);
        assertTrue(rejectedReq.isPresent());
        assertEquals("REJECTED", rejectedReq.get().getStatus());

        // Assert comment is retrievable via tracking view
        driver.get(getBaseUrl() + "/track-request/" + requestId);
        WebElement statusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-badge")));
        assertEquals("REJECTED", statusBadge.getText().trim());

        WebElement reviewCommentElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-comment-text")));
        assertEquals(rejectComment, reviewCommentElem.getText().trim());
    }
}
