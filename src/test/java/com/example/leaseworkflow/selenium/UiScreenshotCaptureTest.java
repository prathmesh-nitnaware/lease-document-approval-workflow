package com.example.leaseworkflow.selenium;

import com.example.leaseworkflow.model.Document;
import com.example.leaseworkflow.model.LeaseRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UiScreenshotCaptureTest extends BaseSeleniumTest {

    private static final Dimension DESKTOP_SIZE = new Dimension(1280, 800);
    private static final Dimension MOBILE_SIZE = new Dimension(375, 667);

    @Test
    @DisplayName("Capture responsive desktop and mobile screenshots for all UI redesign pages")
    void captureAllUiScreenshots() throws IOException, InterruptedException {
        File uiDir = new File("docs/ui");
        if (!uiDir.exists()) {
            uiDir.mkdirs();
        }

        // Seed a sample request so reviewer queue and track request have rich visual state
        LeaseRequest sample = new LeaseRequest();
        sample.setRequesterId("Acme Logistics");
        sample.setLeaseType("Commercial");
        sample.setStatus(LeaseRequest.STATUS_UNDER_REVIEW);
        sample = leaseRequestRepository.save(sample);
        createdRequestIds.add(sample.getId());

        Document doc = new Document();
        doc.setRequestId(sample.getId());
        doc.setFileName("Commercial_Lease_Agreement.pdf");
        doc.setFileType("application/pdf");
        doc.setFileSize(2457600L);
        doc.setValidationResult("VALID");
        documentRepository.save(doc);

        String baseUrl = getBaseUrl();

        // 1. Landing
        capturePage(baseUrl + "/", "landing");

        // 2. Dashboard
        capturePage(baseUrl + "/dashboard", "dashboard");

        // 3. Submit
        capturePage(baseUrl + "/submit-request", "submit");

        // 4. Reviewer Queue
        capturePage(baseUrl + "/reviewer-queue", "reviewer-queue");

        // 5. Track Request
        capturePage(baseUrl + "/track-request/" + sample.getId(), "track-request");

        // 6. Login-less Error Page
        capturePage(baseUrl + "/error-sample-not-found", "error");
    }

    private void capturePage(String url, String name) throws IOException, InterruptedException {
        // Desktop screenshot
        driver.manage().window().setSize(DESKTOP_SIZE);
        driver.get(url);
        Thread.sleep(800);
        File desktopSrc = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File desktopDest = new File("docs/ui/" + name + "-desktop.png");
        Files.copy(desktopSrc.toPath(), desktopDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Mobile screenshot
        driver.manage().window().setSize(MOBILE_SIZE);
        Thread.sleep(500);
        File mobileSrc = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File mobileDest = new File("docs/ui/" + name + "-mobile.png");
        Files.copy(mobileSrc.toPath(), mobileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
