package com.dlocal.slackshot.config;

import com.codeborne.selenide.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class WebDriverConfig {

    @Value("${selenide.browser:chrome}")
    private String browser;

    @Value("${selenide.headless:true}")
    private boolean headless;

    @Value("${selenide.timeout:10000}")
    private long timeout;

    @Value("${selenide.browserSize:1920x1200}")
    private String browserSize;

    @PostConstruct
    public void setupSelenide() {
        // Configure Selenide
        Configuration.browser = browser;
        Configuration.headless = headless;
        Configuration.timeout = timeout;
        Configuration.browserSize = browserSize;
        
        // Additional Chrome options for better compatibility
        Configuration.browserCapabilities.setCapability("chrome.switches", 
            "--no-sandbox,--disable-dev-shm-usage,--disable-gpu,--disable-extensions,--disable-plugins");
        
        // Enable automatic screenshots on failure
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        
        // Set up automatic WebDriver management
        Configuration.driverManagerEnabled = true;
    }
} 