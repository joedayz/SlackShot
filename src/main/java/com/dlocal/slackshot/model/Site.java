package com.dlocal.slackshot.model;

import jakarta.persistence.*;

import java.net.URL;

@Entity
@Table(name = "sites")
public class Site {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String url;
    
    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.NONE;
    
    private String username;
    
    private String password;
    
    public enum LoginType {
        NONE,
        JENKINS,
        GITHUB,
        NEWRELIC
    }
    
    public Site() {}
    
    public Site(String name, String url, LoginType loginType, String username, String password) {
        this.name = name;
        this.url = url;
        this.loginType = loginType;
        this.username = username;
        this.password = password;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public LoginType getLoginType() { return loginType; }
    public void setLoginType(LoginType loginType) { this.loginType = loginType; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getSiteId() {
        return name + "_" + id;
    }
} 