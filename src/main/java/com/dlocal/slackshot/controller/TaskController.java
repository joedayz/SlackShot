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
     * Get all screenshot tasks with detailed information
     */
    @GetMapping("/api/screenshot/tasks/detailed")
    public ResponseEntity<List<ScreenshotTaskDetailResponse>> getScreenshotTasksDetailed() {
        try {
            List<ScreenshotTask> tasks = screenshotTaskRepository.findByActiveTrue();
            List<ScreenshotTaskDetailResponse> detailedTasks = tasks.stream()
                .map(task -> {
                    ScreenshotTaskDetailResponse response = new ScreenshotTaskDetailResponse();
                    response.setId(task.getId());
                    response.setSiteName(task.getSite().getName());
                    response.setSiteUrl(task.getSite().getUrl());
                    response.setScheduledTime(task.getScheduledTime());
                    response.setTaskInterval(task.getTaskInterval());
                    response.setIntervalFormatted(formatDuration(task.getTaskInterval()));
                    response.setActive(task.isActive());
                    response.setCreatedAt(task.getCreatedAt());
                    response.setNextExecution(task.getScheduledTime());
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(detailedTasks);
        } catch (Exception e) {
            log.error("Error getting detailed screenshot tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
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
     * Get all Slack tasks with detailed information
     */
    @GetMapping("/api/slack/tasks/detailed")
    public ResponseEntity<List<SlackTaskDetailResponse>> getSlackTasksDetailed() {
        try {
            List<SlackTask> tasks = slackTaskRepository.findByActiveTrue();
            List<SlackTaskDetailResponse> detailedTasks = tasks.stream()
                .map(task -> {
                    SlackTaskDetailResponse response = new SlackTaskDetailResponse();
                    response.setId(task.getId());
                    response.setSiteName(task.getSite().getName());
                    response.setSiteUrl(task.getSite().getUrl());
                    response.setScheduledTime(task.getScheduledTime());
                    response.setTaskInterval(task.getTaskInterval());
                    response.setIntervalFormatted(formatDuration(task.getTaskInterval()));
                    response.setSlackChannel(task.getSlackChannel());
                    response.setActive(task.isActive());
                    response.setCreatedAt(task.getCreatedAt());
                    response.setNextExecution(task.getScheduledTime());
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(detailedTasks);
        } catch (Exception e) {
            log.error("Error getting detailed Slack tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    /**
     * Get task statistics
     */
    @GetMapping("/api/tasks/stats")
    public ResponseEntity<TaskStatsResponse> getTaskStats() {
        try {
            long totalScreenshotTasks = screenshotTaskRepository.countByActiveTrue();
            long totalSlackTasks = slackTaskRepository.countByActiveTrue();
            long totalInactiveScreenshotTasks = screenshotTaskRepository.countByActiveFalse();
            long totalInactiveSlackTasks = slackTaskRepository.countByActiveFalse();
            
            TaskStatsResponse stats = new TaskStatsResponse();
            stats.setActiveScreenshotTasks(totalScreenshotTasks);
            stats.setActiveSlackTasks(totalSlackTasks);
            stats.setInactiveScreenshotTasks(totalInactiveScreenshotTasks);
            stats.setInactiveSlackTasks(totalInactiveSlackTasks);
            stats.setTotalActiveTasks(totalScreenshotTasks + totalSlackTasks);
            stats.setTotalInactiveTasks(totalInactiveScreenshotTasks + totalInactiveSlackTasks);
            stats.setTotalTasks(totalScreenshotTasks + totalSlackTasks + totalInactiveScreenshotTasks + totalInactiveSlackTasks);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting task stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
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
     * Update screenshot task interval
     */
    @PutMapping("/api/screenshot/task/{id}/interval")
    public ResponseEntity<?> updateScreenshotTaskInterval(@PathVariable("id") Long id, @RequestBody UpdateIntervalRequest request) {
        try {
            Optional<ScreenshotTask> task = screenshotTaskRepository.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ScreenshotTask screenshotTask = task.get();
            screenshotTask.setTaskInterval(request.getInterval());
            screenshotTaskRepository.save(screenshotTask);
            
            log.info("Screenshot task interval updated for ID: {} to: {}", id, request.getInterval());
            return ResponseEntity.ok().body("Screenshot task interval updated successfully");
            
        } catch (Exception e) {
            log.error("Error updating screenshot task interval: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating screenshot task interval: " + e.getMessage());
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

    /**
     * Update Slack task interval
     */
    @PutMapping("/api/slack/task/{id}/interval")
    public ResponseEntity<?> updateSlackTaskInterval(@PathVariable("id") Long id, @RequestBody UpdateIntervalRequest request) {
        try {
            Optional<SlackTask> task = slackTaskRepository.findById(id);
            if (task.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SlackTask slackTask = task.get();
            slackTask.setTaskInterval(request.getInterval());
            slackTaskRepository.save(slackTask);
            
            log.info("Slack task interval updated for ID: {} to: {}", id, request.getInterval());
            return ResponseEntity.ok().body("Slack task interval updated successfully");
            
        } catch (Exception e) {
            log.error("Error updating Slack task interval: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating Slack task interval: " + e.getMessage());
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

    public static class UpdateIntervalRequest {
        private Duration interval;

        public Duration getInterval() { return interval; }
        public void setInterval(Duration interval) { this.interval = interval; }
    }

    public static class ScreenshotTaskDetailResponse {
        private Long id;
        private String siteName;
        private String siteUrl;
        private LocalDateTime scheduledTime;
        private Duration taskInterval;
        private String intervalFormatted;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime nextExecution;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        
        public String getSiteUrl() { return siteUrl; }
        public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }
        
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
        
        public Duration getTaskInterval() { return taskInterval; }
        public void setTaskInterval(Duration taskInterval) { this.taskInterval = taskInterval; }
        
        public String getIntervalFormatted() { return intervalFormatted; }
        public void setIntervalFormatted(String intervalFormatted) { this.intervalFormatted = intervalFormatted; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getNextExecution() { return nextExecution; }
        public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }
    }

    public static class SlackTaskDetailResponse {
        private Long id;
        private String siteName;
        private String siteUrl;
        private LocalDateTime scheduledTime;
        private Duration taskInterval;
        private String intervalFormatted;
        private String slackChannel;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime nextExecution;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        
        public String getSiteUrl() { return siteUrl; }
        public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }
        
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
        
        public Duration getTaskInterval() { return taskInterval; }
        public void setTaskInterval(Duration taskInterval) { this.taskInterval = taskInterval; }
        
        public String getIntervalFormatted() { return intervalFormatted; }
        public void setIntervalFormatted(String intervalFormatted) { this.intervalFormatted = intervalFormatted; }
        
        public String getSlackChannel() { return slackChannel; }
        public void setSlackChannel(String slackChannel) { this.slackChannel = slackChannel; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getNextExecution() { return nextExecution; }
        public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }
    }

    public static class TaskStatsResponse {
        private long activeScreenshotTasks;
        private long activeSlackTasks;
        private long inactiveScreenshotTasks;
        private long inactiveSlackTasks;
        private long totalActiveTasks;
        private long totalInactiveTasks;
        private long totalTasks;

        public long getActiveScreenshotTasks() { return activeScreenshotTasks; }
        public void setActiveScreenshotTasks(long activeScreenshotTasks) { this.activeScreenshotTasks = activeScreenshotTasks; }
        
        public long getActiveSlackTasks() { return activeSlackTasks; }
        public void setActiveSlackTasks(long activeSlackTasks) { this.activeSlackTasks = activeSlackTasks; }
        
        public long getInactiveScreenshotTasks() { return inactiveScreenshotTasks; }
        public void setInactiveScreenshotTasks(long inactiveScreenshotTasks) { this.inactiveScreenshotTasks = inactiveScreenshotTasks; }
        
        public long getInactiveSlackTasks() { return inactiveSlackTasks; }
        public void setInactiveSlackTasks(long inactiveSlackTasks) { this.inactiveSlackTasks = inactiveSlackTasks; }
        
        public long getTotalActiveTasks() { return totalActiveTasks; }
        public void setTotalActiveTasks(long totalActiveTasks) { this.totalActiveTasks = totalActiveTasks; }
        
        public long getTotalInactiveTasks() { return totalInactiveTasks; }
        public void setTotalInactiveTasks(long totalInactiveTasks) { this.totalInactiveTasks = totalInactiveTasks; }
        
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long days = duration.toDays();
        
        if (days > 0) {
            return days + " día" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        } else {
            return "Menos de 1 minuto";
        }
    }
} 