package com.dlocal.slackshot.repository;

import com.dlocal.slackshot.model.SlackTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlackTaskRepository extends JpaRepository<SlackTask, Long> {
    List<SlackTask> findByActiveTrue();
    
    @Query("SELECT t FROM SlackTask t WHERE t.active = true AND t.scheduledTime <= ?1")
    List<SlackTask> findDueTasks(LocalDateTime now);
} 