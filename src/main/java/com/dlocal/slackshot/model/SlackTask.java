package com.dlocal.slackshot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "slack_tasks")
public class SlackTask {
    
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
    
    @Column(nullable = false)
    private String slackToken;
    
    @Column(nullable = false)
    private String slackChannel;
    
    private boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public SlackTask() {}
    
    public SlackTask(Site site, LocalDateTime scheduledTime, Duration taskInterval, String slackToken, String slackChannel, boolean active, LocalDateTime createdAt) {
        this.site = site;
        this.scheduledTime = scheduledTime;
        this.taskInterval = taskInterval;
        this.slackToken = slackToken;
        this.slackChannel = slackChannel;
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
    
    public String getSlackToken() { return slackToken; }
    public void setSlackToken(String slackToken) { this.slackToken = slackToken; }
    
    public String getSlackChannel() { return slackChannel; }
    public void setSlackChannel(String slackChannel) { this.slackChannel = slackChannel; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 