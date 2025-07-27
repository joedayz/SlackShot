package com.dlocal.slackshot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Configuration
public class WebDriverConfig {
    
    @Value("${selenide.browser:chrome}")
    private String browser;
    
    @Value("${selenide.headless:false}")
    private boolean headless;
    
    @Value("${selenide.timeout:30000}")
    private long timeout;
    
    @Value("${selenide.browserSize:1920x1200}")
    private String browserSize;
    
    @PostConstruct
    public void setupSelenide() {
        com.codeborne.selenide.Configuration.browser = browser;
        com.codeborne.selenide.Configuration.headless = headless;
        com.codeborne.selenide.Configuration.timeout = timeout;
        com.codeborne.selenide.Configuration.browserSize = browserSize;
        
        Map<String, Object> chromeOptions = Map.of(
            "args", List.of(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-plugins",
                "--disable-web-security",
                "--allow-running-insecure-content",
                "--remote-debugging-port=9222"
            ),
            "binary", "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        );
        
        com.codeborne.selenide.Configuration.browserCapabilities.setCapability("goog:chromeOptions", chromeOptions);
        com.codeborne.selenide.Configuration.screenshots = true;
        com.codeborne.selenide.Configuration.savePageSource = false;
    }
} 