package com.dlocal.slackshot.repository;

import com.dlocal.slackshot.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByName(String name);
    boolean existsByName(String name);
} 