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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ScreenshotService {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotService.class);

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
            open(site.getUrl());
            
            if (site.getLoginType() != LoginType.NONE) {
                handleLogin(site);
            }
            
            Selenide.sleep(3000);
            
            byte[] screenshotBytes = ((org.openqa.selenium.TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            
            Screenshot screenshot = new Screenshot();
            screenshot.setName(site.getName());
            screenshot.setUrl(site.getUrl());
            screenshot.setType("png");
            screenshot.setCreatedAt(LocalDateTime.now());
            screenshot.setImageData(screenshotBytes);
            screenshot.setSite(site);
            
            Screenshot savedScreenshot = screenshotRepository.save(screenshot);
            log.info("Screenshot saved with ID: {}", savedScreenshot.getId());
            
            return savedScreenshot;
            
        } catch (Exception e) {
            log.error("Error taking screenshot for site: {}", site.getName(), e);
            throw new RuntimeException("Failed to take screenshot", e);
        } finally {
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
            throw e;
        }
    }
    
    private void handleJenkinsLogin(Site site) {
        log.info("Handling Jenkins login for user: {}", site.getUsername());
        try {
            $("input[name='j_username']").shouldBe(visible).setValue(site.getUsername());
            $("input[name='j_password']").shouldBe(visible).setValue(site.getPassword());
            $("input[type='submit']").shouldBe(visible).click();
            Selenide.sleep(3000);
            log.info("Jenkins login completed successfully");
        } catch (Exception e) {
            log.error("Error during Jenkins login", e);
            throw e;
        }
    }
    
    private void handleGithubLogin(Site site) {
        log.info("Handling GitHub login for user: {}", site.getUsername());
        try {
            $("input[name='login']").shouldBe(visible).setValue(site.getUsername());
            $("input[name='password']").shouldBe(visible).setValue(site.getPassword());
            $("input[type='submit']").shouldBe(visible).click();
            Selenide.sleep(3000);
            log.info("GitHub login completed successfully");
        } catch (Exception e) {
            log.error("Error during GitHub login", e);
            throw e;
        }
    }
    
    private void handleNewRelicLogin(Site site) {
        log.info("Handling New Relic login for user: {}", site.getUsername());
        try {
            $("input[type='email']").shouldBe(visible).setValue(site.getUsername());
            $("button[type='submit']").shouldBe(visible).click();
            Selenide.sleep(5000);
            if (WebDriverRunner.url().contains("accounts.google.com")) {
                log.info("Google SSO detected, waiting for automatic redirect...");
                Selenide.sleep(10000);
            }
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
    @Scheduled(fixedRate = 60000)
    public void processScreenshotTasks() {
        log.debug("Checking for due screenshot tasks...");
        
        List<ScreenshotTask> dueTasks = taskRepository.findDueTasks(LocalDateTime.now());
        
        for (ScreenshotTask task : dueTasks) {
            try {
                log.info("Processing screenshot task for site: {} at {} with interval: {}", 
                    task.getSite().getName(), task.getScheduledTime(), task.getTaskInterval());
                
                takeScreenshot(task.getSite());
                
                task.setScheduledTime(task.getScheduledTime().plus(task.getTaskInterval()));
                taskRepository.save(task);
                
                log.info("Screenshot task completed for site: {}", task.getSite().getName());
                
            } catch (Exception e) {
                log.error("Error processing screenshot task for site: {}", task.getSite().getName(), e);
            }
        }
    }
    
    /**
     * Get the latest screenshot for a given site
     */
    public Screenshot getLatestScreenshot(String siteName) {
        return screenshotRepository.findLatestBySiteName(siteName)
            .orElseThrow(() -> new RuntimeException("No screenshot found for site: " + siteName));
    }
    
    /**
     * Take a screenshot immediately for a given site
     */
    public Screenshot takeScreenshotNow(String siteName) {
        Site site = siteRepository.findByName(siteName)
            .orElseThrow(() -> new RuntimeException("Site not found: " + siteName));
        return takeScreenshot(site);
    }
} 