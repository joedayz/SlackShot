package com.dlocal.slackshot.service;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

@Service
public class WebDriverManager {

    private static final Logger log = LoggerFactory.getLogger(WebDriverManager.class);

    @Value("${webdriver.pool.max-size:5}")
    private int maxPoolSize;

    @Value("${webdriver.pool.timeout-seconds:30}")
    private int timeoutSeconds;

    private final ConcurrentLinkedQueue<WebDriver> availableDrivers = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Thread, WebDriver> activeDrivers = new ConcurrentHashMap<>();
    private final AtomicInteger totalDrivers = new AtomicInteger(0);

    public WebDriver getDriver() {
        WebDriver currentDriver = activeDrivers.get(Thread.currentThread());
        if (currentDriver != null) {
            return currentDriver;
        }

        WebDriver driver = availableDrivers.poll();
        if (driver != null) {
            log.debug("Reusing WebDriver from pool");
            activeDrivers.put(Thread.currentThread(), driver);
            return driver;
        }

        if (totalDrivers.get() < maxPoolSize) {
            driver = createNewDriver();
            if (driver != null) {
                log.debug("Created new WebDriver, total: {}", totalDrivers.get());
                activeDrivers.put(Thread.currentThread(), driver);
                return driver;
            }
        }

        log.warn("WebDriver pool is full, waiting for available driver...");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
            driver = availableDrivers.poll();
            if (driver != null) {
                log.debug("Got WebDriver after waiting");
                activeDrivers.put(Thread.currentThread(), driver);
                return driver;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for WebDriver", e);
            }
        }

        throw new RuntimeException("Timeout waiting for available WebDriver");
    }

    public void releaseDriver() {
        WebDriver driver = activeDrivers.remove(Thread.currentThread());
        if (driver != null) {
            try {
                driver.manage().deleteAllCookies();
                driver.manage().window().maximize();
                
                if (availableDrivers.size() < maxPoolSize) {
                    availableDrivers.offer(driver);
                    log.debug("Returned WebDriver to pool, available: {}", availableDrivers.size());
                } else {
                    closeDriver(driver);
                    totalDrivers.decrementAndGet();
                    log.debug("Closed WebDriver, pool was full");
                }
            } catch (Exception e) {
                log.error("Error preparing WebDriver for reuse", e);
                closeDriver(driver);
                totalDrivers.decrementAndGet();
            }
        }
    }

    private WebDriver createNewDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            
            List<String> args = new ArrayList<>(List.of(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-plugins",
                "--headless",
                "--remote-debugging-port=0"
            ));
            
            options.addArguments(args);
            
            WebDriver driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            
            totalDrivers.incrementAndGet();
            log.debug("Created new WebDriver instance");
            
            return driver;
        } catch (Exception e) {
            log.error("Failed to create new WebDriver", e);
            return null;
        }
    }

    private void closeDriver(WebDriver driver) {
        try {
            if (driver != null) {
                driver.quit();
                log.debug("Closed WebDriver instance");
            }
        } catch (Exception e) {
            log.error("Error closing WebDriver", e);
        }
    }

    public PoolStats getPoolStats() {
        return new PoolStats(
            totalDrivers.get(),
            activeDrivers.size(),
            availableDrivers.size(),
            maxPoolSize
        );
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down WebDriver pool...");
        
        activeDrivers.values().forEach(this::closeDriver);
        activeDrivers.clear();
        
        WebDriver driver;
        while ((driver = availableDrivers.poll()) != null) {
            closeDriver(driver);
        }
        
        totalDrivers.set(0);
        log.info("WebDriver pool shutdown complete");
    }

    public static class PoolStats {
        private final int totalDrivers;
        private final int activeDrivers;
        private final int availableDrivers;
        private final int maxPoolSize;

        public PoolStats(int totalDrivers, int activeDrivers, int availableDrivers, int maxPoolSize) {
            this.totalDrivers = totalDrivers;
            this.activeDrivers = activeDrivers;
            this.availableDrivers = availableDrivers;
            this.maxPoolSize = maxPoolSize;
        }

        public int getTotalDrivers() { return totalDrivers; }
        public int getActiveDrivers() { return activeDrivers; }
        public int getAvailableDrivers() { return availableDrivers; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getUtilizationPercentage() { 
            return maxPoolSize > 0 ? (totalDrivers * 100) / maxPoolSize : 0; 
        }
    }
} 