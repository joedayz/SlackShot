package com.dlocal.slackshot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "slack_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlackTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(nullable = false)
    private Duration interval;
    
    @Column(nullable = false)
    private String slackToken;
    
    @Column(nullable = false)
    private String slackChannel;
    
    private boolean active = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
} 