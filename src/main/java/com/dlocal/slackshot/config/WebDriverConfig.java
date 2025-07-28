package com.dlocal.slackshot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
    
    @Value("${selenide.disableSecurity:false}")
    private boolean disableSecurity;
    
    @PostConstruct
    public void setupSelenide() {
        com.codeborne.selenide.Configuration.browser = browser;
        com.codeborne.selenide.Configuration.headless = headless;
        com.codeborne.selenide.Configuration.timeout = timeout;
        com.codeborne.selenide.Configuration.browserSize = browserSize;
        
        String chromeBinary = findChromeBinary();
        
        List<String> chromeArgs = new ArrayList<>(List.of(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-extensions",
            "--disable-plugins",
            "--remote-debugging-port=9222"
        ));
        
        if (disableSecurity) {
            chromeArgs.add("--disable-web-security");
            chromeArgs.add("--allow-running-insecure-content");
        }
        
        Map<String, Object> chromeOptions = Map.of(
            "args", chromeArgs,
            "binary", chromeBinary
        );
        
        com.codeborne.selenide.Configuration.browserCapabilities.setCapability("goog:chromeOptions", chromeOptions);
        com.codeborne.selenide.Configuration.screenshots = true;
        com.codeborne.selenide.Configuration.savePageSource = false;
    }
    
    private String findChromeBinary() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("mac")) {
            return findChromeOnMac();
        } else if (os.contains("linux")) {
            return findChromeOnLinux();
        } else if (os.contains("windows")) {
            return findChromeOnWindows();
        } else {
            return "google-chrome";
        }
    }
    
    private String findChromeOnMac() {
        String[] possiblePaths = {
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/Applications/Chromium.app/Contents/MacOS/Chromium",
            "/usr/bin/google-chrome",
            "/usr/bin/chromium"
        };
        
        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        
        return "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
    }
    
    private String findChromeOnLinux() {
        String[] possiblePaths = {
            "/usr/bin/google-chrome",
            "/usr/bin/google-chrome-stable",
            "/usr/bin/chromium-browser",
            "/usr/bin/chromium",
            "/snap/bin/google-chrome",
            "/opt/google/chrome/chrome"
        };
        
        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        
        return "google-chrome";
    }
    
    private String findChromeOnWindows() {
        String[] possiblePaths = {
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe"
        };
        
        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        
        return "chrome.exe";
    }
} 