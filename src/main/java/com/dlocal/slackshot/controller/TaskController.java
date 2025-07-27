package com.dlocal.slackshot.controller;

import com.dlocal.slackshot.model.ScreenshotTask;
import com.dlocal.slackshot.model.SlackTask;
import com.dlocal.slackshot.model.Site;
import com.dlocal.slackshot.repository.ScreenshotTaskRepository;
import com.dlocal.slackshot.repository.SlackTaskRepository;
import com.dlocal.slackshot.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ScreenshotTaskRepository screenshotTaskRepository;
    
    @Autowired
    private SlackTaskRepository slackTaskRepository;
    
    @Autowired
    private SiteRepository siteRepository;

    /**
     * Add a new screenshot task
     */
    @PutMapping("/api/screenshot/task")
    public ResponseEntity<?> addScreenshotTask(@Valid @RequestBody ScreenshotTaskRequest request) {
        try {
            Optional<Site> site = siteRepository.findByName(request.getSiteName());
            if (site.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Site not found: " + request.getSiteName());
            }
            
            ScreenshotTask task = new ScreenshotTask();
            task.setSite(site.get());
            task.setScheduledTime(request.getTime());
            task.setTaskInterval(request.getInterval());
            task.setActive(true);
            task.setCreatedAt(LocalDateTime.now());
            
            ScreenshotTask savedTask = screenshotTaskRepository.save(task);
            log.info("Screenshot task added for site: {}", request.getSiteName());
            
            return ResponseEntity.ok().body("Screenshot task added successfully");
            
        } catch (Exception e) {
            log.error("Error adding screenshot task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding screenshot task: " + e.getMessage());
        }
    }

    /**
     * Add a new Slack task
     */
    @PutMapping("/api/slack/task")
    public ResponseEntity<?> addSlackTask(@Valid @RequestBody SlackTaskRequest request) {
        try {
            Optional<Site> site = siteRepository.findByName(request.getSiteName());
            if (site.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Site not found: " + request.getSiteName());
            }
            
            SlackTask task = new SlackTask();
            task.setSite(site.get());
            task.setScheduledTime(request.getTime());
            task.setTaskInterval(request.getInterval());
            task.setSlackToken(request.getSlackToken());
            task.setSlackChannel(request.getSlackChannel());
            task.setActive(true);
            task.setCreatedAt(LocalDateTime.now());
            
            SlackTask savedTask = slackTaskRepository.save(task);
            log.info("Slack task added for site: {}", request.getSiteName());
            
            return ResponseEntity.ok().body("Slack task added successfully");
            
        } catch (Exception e) {
            log.error("Error adding Slack task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding Slack task: " + e.getMessage());
        }
    }

    /**
     * Get all screenshot tasks
     */
    @GetMapping("/api/screenshot/tasks")
    public ResponseEntity<List<ScreenshotTask>> getAllScreenshotTasks() {
        List<ScreenshotTask> tasks = screenshotTaskRepository.findAll();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get all Slack tasks
     */
    @GetMapping("/api/slack/tasks")
    public ResponseEntity<List<SlackTask>> getAllSlackTasks() {
        List<SlackTask> tasks = slackTaskRepository.findAll();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Delete a screenshot task
     */
    @DeleteMapping("/api/screenshot/task/{id}")
    public ResponseEntity<?> deleteScreenshotTask(@PathVariable("id") Long id) {
        try {
            Optional<ScreenshotTask> task = screenshotTaskRepository.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ScreenshotTask screenshotTask = task.get();
            screenshotTask.setActive(false);
            screenshotTaskRepository.save(screenshotTask);
            
            log.info("Screenshot task deactivated: {}", id);
            return ResponseEntity.ok().body("Screenshot task deactivated successfully");
            
        } catch (Exception e) {
            log.error("Error deactivating screenshot task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deactivating screenshot task: " + e.getMessage());
        }
    }

    /**
     * Delete a Slack task
     */
    @DeleteMapping("/api/slack/task/{id}")
    public ResponseEntity<?> deleteSlackTask(@PathVariable("id") Long id) {
        try {
            Optional<SlackTask> task = slackTaskRepository.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SlackTask slackTask = task.get();
            slackTask.setActive(false);
            slackTaskRepository.save(slackTask);
            
            log.info("Slack task deactivated: {}", id);
            return ResponseEntity.ok().body("Slack task deactivated successfully");
            
        } catch (Exception e) {
            log.error("Error deactivating Slack task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deactivating Slack task: " + e.getMessage());
        }
    }

    public static class ScreenshotTaskRequest {
        private String siteName;
        private LocalDateTime time;
        private Duration interval;

        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }
    }

    public static class SlackTaskRequest {
        private String siteName;
        private LocalDateTime time;
        private Duration interval;
        private String slackToken;
        private String slackChannel;

        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }
        public String getSlackToken() { return slackToken; }
        public void setSlackToken(String slackToken) { this.slackToken = slackToken; }
        public String getSlackChannel() { return slackChannel; }
        public void setSlackChannel(String slackChannel) { this.slackChannel = slackChannel; }
    }
} 