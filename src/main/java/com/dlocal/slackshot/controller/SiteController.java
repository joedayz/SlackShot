package com.dlocal.slackshot.controller;

import com.dlocal.slackshot.model.Screenshot;
import com.dlocal.slackshot.model.Site;
import com.dlocal.slackshot.model.ScreenshotTask;
import com.dlocal.slackshot.model.SlackTask;
import com.dlocal.slackshot.repository.ScreenshotRepository;
import com.dlocal.slackshot.repository.SiteRepository;
import com.dlocal.slackshot.repository.ScreenshotTaskRepository;
import com.dlocal.slackshot.repository.SlackTaskRepository;
import com.dlocal.slackshot.service.ScreenshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private static final Logger log = LoggerFactory.getLogger(SiteController.class);

    @Autowired
    private SiteRepository siteRepository;
    
    @Autowired
    private ScreenshotRepository screenshotRepository;
    
    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private ScreenshotTaskRepository screenshotTaskRepository;

    @Autowired
    private SlackTaskRepository slackTaskRepository;

    /**
     * Add a new site
     */
    @PutMapping
    public ResponseEntity<?> addSite(@Valid @RequestBody Site site) {
        try {
            if (site.getLoginType() != Site.LoginType.NONE) {
                if (site.getUsername() == null || site.getUsername().isEmpty()) {
                    return ResponseEntity.badRequest().body("Username is required for login type: " + site.getLoginType());
                }
                if (site.getLoginType() == Site.LoginType.JENKINS || site.getLoginType() == Site.LoginType.GITHUB) {
                    if (site.getPassword() == null || site.getPassword().isEmpty()) {
                        return ResponseEntity.badRequest().body("Password is required for login type: " + site.getLoginType());
                    }
                }
            }
            
            if (siteRepository.existsByName(site.getName())) {
                return ResponseEntity.badRequest().body("Site with name '" + site.getName() + "' already exists");
            }
            
            Site savedSite = siteRepository.save(site);
            log.info("Site added successfully: {}", savedSite.getName());
            
            return ResponseEntity.ok().body("Site added successfully");
            
        } catch (Exception e) {
            log.error("Error adding site", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding site: " + e.getMessage());
        }
    }

    /**
     * Get latest screenshot for a site
     */
    @GetMapping
    public ResponseEntity<?> getScreenshot(@RequestParam("name") String name) {
        try {
            Screenshot screenshot = screenshotService.getLatestScreenshot(name);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(screenshot.getImageData());
        } catch (Exception e) {
            log.error("Error getting screenshot for site: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting screenshot: " + e.getMessage());
        }
    }

    /**
     * Take a new screenshot immediately
     */
    @PostMapping("/{name}/screenshot")
    public ResponseEntity<?> takeScreenshotNow(@PathVariable("name") String name) {
        try {
            Screenshot screenshot = screenshotService.takeScreenshotNow(name);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(screenshot.getImageData());
        } catch (Exception e) {
            log.error("Error taking screenshot for site: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error taking screenshot: " + e.getMessage());
        }
    }

    /**
     * Get all sites
     */
    @GetMapping("/list")
    public ResponseEntity<List<Site>> getAllSites() {
        List<Site> sites = siteRepository.findAll();
        return ResponseEntity.ok(sites);
    }

    /**
     * Get sites with task information
     */
    @GetMapping("/list/with-tasks")
    public ResponseEntity<List<SiteWithTasksResponse>> getSitesWithTasks() {
        try {
            List<Site> sites = siteRepository.findAll();
            List<SiteWithTasksResponse> sitesWithTasks = sites.stream()
                .map(site -> {
                    SiteWithTasksResponse response = new SiteWithTasksResponse();
                    response.setSite(site);
                    
                    List<ScreenshotTask> screenshotTasks = screenshotTaskRepository.findBySiteAndActiveTrue(site);
                    List<SlackTask> slackTasks = slackTaskRepository.findBySiteAndActiveTrue(site);
                    
                    response.setScreenshotTasks(screenshotTasks);
                    response.setSlackTasks(slackTasks);
                    response.setTotalTasks(screenshotTasks.size() + slackTasks.size());
                    
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(sitesWithTasks);
        } catch (Exception e) {
            log.error("Error getting sites with tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    /**
     * Get site statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<SiteStatsResponse> getSiteStats() {
        try {
            long totalSites = siteRepository.count();
            long totalScreenshotTasks = screenshotTaskRepository.countByActiveTrue();
            long totalSlackTasks = slackTaskRepository.countByActiveTrue();
            long totalScreenshots = screenshotRepository.count();
            
            SiteStatsResponse stats = new SiteStatsResponse();
            stats.setTotalSites(totalSites);
            stats.setTotalScreenshotTasks(totalScreenshotTasks);
            stats.setTotalSlackTasks(totalSlackTasks);
            stats.setTotalScreenshots(totalScreenshots);
            stats.setTotalTasks(totalScreenshotTasks + totalSlackTasks);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting site stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    /**
     * Get site by name
     */
    @GetMapping("/{name}")
    public ResponseEntity<Site> getSiteByName(@PathVariable("name") String name) {
        Optional<Site> site = siteRepository.findByName(name);
        return site.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a site
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteSite(@PathVariable("name") String name) {
        try {
            Optional<Site> site = siteRepository.findByName(name);
            if (site.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            siteRepository.delete(site.get());
            log.info("Site deleted successfully: {}", name);
            
            return ResponseEntity.ok().body("Site deleted successfully");
            
        } catch (Exception e) {
            log.error("Error deleting site: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting site: " + e.getMessage());
        }
    }

    public static class SiteWithTasksResponse {
        private Site site;
        private List<ScreenshotTask> screenshotTasks;
        private List<SlackTask> slackTasks;
        private int totalTasks;

        public Site getSite() { return site; }
        public void setSite(Site site) { this.site = site; }
        
        public List<ScreenshotTask> getScreenshotTasks() { return screenshotTasks; }
        public void setScreenshotTasks(List<ScreenshotTask> screenshotTasks) { this.screenshotTasks = screenshotTasks; }
        
        public List<SlackTask> getSlackTasks() { return slackTasks; }
        public void setSlackTasks(List<SlackTask> slackTasks) { this.slackTasks = slackTasks; }
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    }

    public static class SiteStatsResponse {
        private long totalSites;
        private long totalScreenshotTasks;
        private long totalSlackTasks;
        private long totalScreenshots;
        private long totalTasks;

        public long getTotalSites() { return totalSites; }
        public void setTotalSites(long totalSites) { this.totalSites = totalSites; }
        
        public long getTotalScreenshotTasks() { return totalScreenshotTasks; }
        public void setTotalScreenshotTasks(long totalScreenshotTasks) { this.totalScreenshotTasks = totalScreenshotTasks; }
        
        public long getTotalSlackTasks() { return totalSlackTasks; }
        public void setTotalSlackTasks(long totalSlackTasks) { this.totalSlackTasks = totalSlackTasks; }
        
        public long getTotalScreenshots() { return totalScreenshots; }
        public void setTotalScreenshots(long totalScreenshots) { this.totalScreenshots = totalScreenshots; }
        
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    }
} 