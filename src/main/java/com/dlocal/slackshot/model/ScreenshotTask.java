package com.dlocal.slackshot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "screenshot_tasks")
public class ScreenshotTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(name = "task_interval", nullable = false)
    private Duration taskInterval;
    
    private boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public ScreenshotTask() {}
    
    public ScreenshotTask(Site site, LocalDateTime scheduledTime, Duration taskInterval, boolean active, LocalDateTime createdAt) {
        this.site = site;
        this.scheduledTime = scheduledTime;
        this.taskInterval = taskInterval;
        this.active = active;
        this.createdAt = createdAt;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }
    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Duration getTaskInterval() { return taskInterval; }
    public void setTaskInterval(Duration taskInterval) { this.taskInterval = taskInterval; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 