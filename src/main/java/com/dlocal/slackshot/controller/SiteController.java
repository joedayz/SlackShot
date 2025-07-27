package com.dlocal.slackshot.controller;

import com.dlocal.slackshot.model.Screenshot;
import com.dlocal.slackshot.model.Site;
import com.dlocal.slackshot.repository.SiteRepository;
import com.dlocal.slackshot.service.ScreenshotService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SiteController {

    @Autowired
    private SiteRepository siteRepository;
    
    @Autowired
    private ScreenshotService screenshotService;

    /**
     * Add a new site
     */
    @PutMapping
    public ResponseEntity<?> addSite(@Valid @RequestBody Site site) {
        try {
            // Validate login credentials if login type is specified
            if (site.getLoginType() != Site.LoginType.NONE) {
                if (site.getUsername() == null || site.getUsername().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body("Username is required for login type: " + site.getLoginType());
                }
                if (site.getPassword() == null || site.getPassword().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body("Password is required for login type: " + site.getLoginType());
                }
            }
            
            // Check if site name already exists
            if (siteRepository.existsByName(site.getName())) {
                return ResponseEntity.badRequest()
                    .body("Site with name '" + site.getName() + "' already exists");
            }
            
            Site savedSite = siteRepository.save(site);
            log.info("Site added: {}", savedSite.getName());
            
            return ResponseEntity.ok().body("Site added successfully");
            
        } catch (Exception e) {
            log.error("Error adding site", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding site: " + e.getMessage());
        }
    }

    /**
     * Get a screenshot for a site
     */
    @GetMapping
    public ResponseEntity<?> getScreenshot(@RequestParam String name) {
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
     * Take a new screenshot immediately for a site
     */
    @PostMapping("/{name}/screenshot")
    public ResponseEntity<?> takeScreenshotNow(@PathVariable String name) {
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
     * Get site by name
     */
    @GetMapping("/{name}")
    public ResponseEntity<Site> getSiteByName(@PathVariable String name) {
        Optional<Site> site = siteRepository.findByName(name);
        return site.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a site
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteSite(@PathVariable String name) {
        try {
            Optional<Site> site = siteRepository.findByName(name);
            if (site.isPresent()) {
                siteRepository.delete(site.get());
                log.info("Site deleted: {}", name);
                return ResponseEntity.ok().body("Site deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting site: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting site: " + e.getMessage());
        }
    }
} 