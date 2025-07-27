package com.dlocal.slackshot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.net.URL;

@Entity
@Table(name = "sites")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    public String getId() {
        return name + "_" + id;
    }
} 