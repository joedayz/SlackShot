package com.dlocal.slackshot.service;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.dlocal.slackshot.model.Screenshot;
import com.dlocal.slackshot.model.ScreenshotTask;
import com.dlocal.slackshot.model.Site;
import com.dlocal.slackshot.model.Site.LoginType;
import com.dlocal.slackshot.repository.ScreenshotRepository;
import com.dlocal.slackshot.repository.ScreenshotTaskRepository;
import com.dlocal.slackshot.repository.SiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@Service
@Slf4j
public class ScreenshotService {

    @Autowired
    private ScreenshotRepository screenshotRepository;
    
    @Autowired
    private ScreenshotTaskRepository taskRepository;
    
    @Autowired
    private SiteRepository siteRepository;

    /**
     * Takes a screenshot of the given site using Selenide
     */
    public Screenshot takeScreenshot(Site site) {
        log.info("Taking screenshot for site: {}", site.getName());
        
        try {
            // Open the site
            open(site.getUrl());
            
            // Handle login if required
            if (site.getLoginType() != LoginType.NONE) {
                handleLogin(site);
            }
            
            // Wait for page to load completely
            Selenide.sleep(3000);
            
            // Take screenshot using Selenide
            byte[] screenshotBytes = Selenide.screenshotAsBytes();
            
            // Create screenshot entity
            Screenshot screenshot = new Screenshot();
            screenshot.setName(site.getName());
            screenshot.setUrl(site.getUrl());
            screenshot.setType("png");
            screenshot.setCreatedAt(LocalDateTime.now());
            screenshot.setImageData(screenshotBytes);
            screenshot.setSite(site);
            
            // Save to database
            Screenshot savedScreenshot = screenshotRepository.save(screenshot);
            log.info("Screenshot saved with ID: {}", savedScreenshot.getId());
            
            return savedScreenshot;
            
        } catch (Exception e) {
            log.error("Error taking screenshot for site: {}", site.getName(), e);
            throw new RuntimeException("Failed to take screenshot", e);
        } finally {
            // Close browser
            if (WebDriverRunner.hasWebDriverStarted()) {
                WebDriverRunner.closeWebDriver();
            }
        }
    }
    
    private void handleLogin(Site site) {
        try {
            switch (site.getLoginType()) {
                case JENKINS:
                    handleJenkinsLogin(site);
                    break;
                case GITHUB:
                    handleGithubLogin(site);
                    break;
                case NEWRELIC:
                    handleNewRelicLogin(site);
                    break;
                default:
                    log.warn("Unknown login type: {}", site.getLoginType());
            }
        } catch (Exception e) {
            log.error("Error during login for site: {}", site.getName(), e);
        }
    }
    
    private void handleJenkinsLogin(Site site) {
        // Wait for username field and fill it
        $("#j_username")
            .shouldBe(visible)
            .setValue(site.getUsername());
        
        // Find password field and submit
        $("[name='j_password']")
            .shouldBe(visible)
            .setValue(site.getPassword())
            .pressEnter();
        
        // Wait for login to complete
        Selenide.sleep(3000);
    }
    
    private void handleGithubLogin(Site site) {
        // Wait for login field and fill it
        $("#login_field")
            .shouldBe(visible)
            .setValue(site.getUsername());
        
        // Find password field and submit
        $("#password")
            .shouldBe(visible)
            .setValue(site.getPassword())
            .pressEnter();
        
        // Wait for login to complete
        Selenide.sleep(3000);
    }
    
    private void handleNewRelicLogin(Site site) {
        log.info("Handling New Relic login for user: {}", site.getUsername());
        
        try {
            // Wait for email input field and fill it
            $("input[type='email']")
                .shouldBe(visible)
                .setValue(site.getUsername());
            
            // Click the Next button
            $("button[type='submit']")
                .shouldBe(visible)
                .click();
            
            // Wait for Google SSO redirect or direct login
            Selenide.sleep(5000);
            
            // Check if we need to handle Google SSO
            if (WebDriverRunner.url().contains("accounts.google.com")) {
                log.info("Google SSO detected, waiting for automatic redirect...");
                Selenide.sleep(10000); // Wait longer for SSO completion
            }
            
            // Wait for dashboard to load
            Selenide.sleep(5000);
            
            log.info("New Relic login completed successfully");
            
        } catch (Exception e) {
            log.error("Error during New Relic login", e);
            throw e;
        }
    }
    
    /**
     * Scheduled task that runs every minute to check for due screenshot tasks
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScreenshotTasks() {
        log.debug("Checking for due screenshot tasks...");
        
        List<ScreenshotTask> dueTasks = taskRepository.findDueTasks(LocalDateTime.now());
        
        for (ScreenshotTask task : dueTasks) {
            try {
                log.info("Processing screenshot task for site: {}", task.getSite().getName());
                
                // Take screenshot
                Screenshot screenshot = takeScreenshot(task.getSite());
                
                // Update next scheduled time
                task.setScheduledTime(task.getScheduledTime().plus(task.getInterval()));
                taskRepository.save(task);
                
                log.info("Screenshot task completed for site: {}", task.getSite().getName());
                
            } catch (Exception e) {
                log.error("Error processing screenshot task for site: {}", task.getSite().getName(), e);
            }
        }
    }
    
    /**
     * Get the latest screenshot for a site
     */
    public Screenshot getLatestScreenshot(String siteName) {
        Site site = siteRepository.findByName(siteName)
            .orElseThrow(() -> new RuntimeException("Site not found: " + siteName));
            
        return screenshotRepository.findFirstBySiteOrderByCreatedAtDesc(site)
            .orElseThrow(() -> new RuntimeException("No screenshots found for site: " + siteName));
    }
    
    /**
     * Take a screenshot immediately for a specific site
     */
    public Screenshot takeScreenshotNow(String siteName) {
        Site site = siteRepository.findByName(siteName)
            .orElseThrow(() -> new RuntimeException("Site not found: " + siteName));
        
        return takeScreenshot(site);
    }
} 