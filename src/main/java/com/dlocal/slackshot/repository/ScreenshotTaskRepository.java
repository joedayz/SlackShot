package com.dlocal.slackshot.repository;

import com.dlocal.slackshot.model.ScreenshotTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreenshotTaskRepository extends JpaRepository<ScreenshotTask, Long> {
    List<ScreenshotTask> findByActiveTrue();
    
    @Query("SELECT t FROM ScreenshotTask t WHERE t.active = true AND t.scheduledTime <= ?1")
    List<ScreenshotTask> findDueTasks(LocalDateTime now);
} 