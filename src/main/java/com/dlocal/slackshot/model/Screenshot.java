package com.dlocal.slackshot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screenshots")
public class Screenshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String url;
    
    private String type = "png";
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;
    
    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;
    
    public Screenshot() {}
    
    public Screenshot(String name, String url, String type, LocalDateTime createdAt, byte[] imageData, Site site) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.createdAt = createdAt;
        this.imageData = imageData;
        this.site = site;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    
    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }
    
    public String getScreenshotId() {
        return name + "_" + createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
    }
} 