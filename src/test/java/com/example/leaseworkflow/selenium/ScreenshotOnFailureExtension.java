package com.example.leaseworkflow.selenium;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback, AfterEachCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            captureScreenshot(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof BaseSeleniumTest baseTest) {
            baseTest.quitDriver();
        }
    }

    private void captureScreenshot(ExtensionContext context) {
        Object testInstance = context.getRequiredTestInstance();
        if (testInstance instanceof BaseSeleniumTest baseTest) {
            WebDriver driver = baseTest.getDriver();
            if (driver instanceof TakesScreenshot takesScreenshot) {
                try {
                    File screenshotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
                    String className = context.getRequiredTestClass().getSimpleName();
                    String methodName = context.getRequiredTestMethod().getName();

                    File targetDir = new File("target/selenium-screenshots");
                    if (!targetDir.exists()) {
                        targetDir.mkdirs();
                    }

                    File destinationFile = new File(targetDir, className + "_" + methodName + ".png");
                    Files.copy(screenshotFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[ScreenshotOnFailureExtension] Screenshot successfully saved to: " + destinationFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("[ScreenshotOnFailureExtension] Failed to capture screenshot: " + e.getMessage());
                }
            }
        }
    }
}
