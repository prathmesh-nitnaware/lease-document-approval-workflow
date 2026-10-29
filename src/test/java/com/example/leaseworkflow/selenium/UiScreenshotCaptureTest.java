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

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Tag("screenshot")
@ActiveProfiles({"dev", "demo"})
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

        // 1. Landing Page
        capturePage(baseUrl + "/", "landing");

        // 2. Login Page
        capturePage(baseUrl + "/login", "login");

        // 3. Dashboard (as Legal Reviewer)
        capturePage(baseUrl + "/quick-login?personaId=sarah.legal&redirectUrl=/dashboard", "dashboard");

        // 4. Submit Request (as Prathmesh, Tenant)
        capturePage(baseUrl + "/quick-login?personaId=john.tenant&redirectUrl=/submit-request", "submit");

        // 5. Reviewer Queue (as Legal Reviewer)
        capturePage(baseUrl + "/quick-login?personaId=sarah.legal&redirectUrl=/reviewer-queue", "reviewer-queue");

        // 6. Track Request Details
        capturePage(baseUrl + "/track-request/" + sample.getId(), "track-request");
    }

    private void capturePage(String url, String name) throws IOException, InterruptedException {
        // Desktop screenshot
        driver.manage().window().setSize(DESKTOP_SIZE);
        driver.get(url);
        Thread.sleep(1500);
        File desktopSrc = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File desktopDest = new File("docs/ui/" + name + "-desktop.png");
        Files.copy(desktopSrc.toPath(), desktopDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Mobile screenshot
        driver.manage().window().setSize(MOBILE_SIZE);
        Thread.sleep(1000);
        File mobileSrc = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File mobileDest = new File("docs/ui/" + name + "-mobile.png");
        Files.copy(mobileSrc.toPath(), mobileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
