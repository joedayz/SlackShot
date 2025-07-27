package com.dlocal.slackshot.repository;

import com.dlocal.slackshot.model.Screenshot;
import com.dlocal.slackshot.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenshotRepository extends JpaRepository<Screenshot, Long> {
    List<Screenshot> findBySiteOrderByCreatedAtDesc(Site site);
    Optional<Screenshot> findFirstBySiteOrderByCreatedAtDesc(Site site);
    Optional<Screenshot> findBySiteAndId(Site site, Long id);
} 