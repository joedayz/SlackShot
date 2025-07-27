package com.dlocal.slackshot.service;

import com.dlocal.slackshot.model.Screenshot;
import com.dlocal.slackshot.model.SlackTask;
import com.dlocal.slackshot.repository.SlackTaskRepository;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.files.FilesUploadRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.files.FilesUploadResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SlackService {

    private static final Logger log = LoggerFactory.getLogger(SlackService.class);

    @Autowired
    private SlackTaskRepository slackTaskRepository;
    
    @Autowired
    private ScreenshotService screenshotService;

    private final Slack slack = Slack.getInstance();

    /**
     * Send a screenshot to Slack
     */
    public void sendScreenshotToSlack(Screenshot screenshot, String slackToken, String channel) {
        try {
            log.info("Sending screenshot to Slack channel: {}", channel);
            
            // Upload file to Slack
            FilesUploadRequest uploadRequest = FilesUploadRequest.builder()
                .token(slackToken)
                .channels(List.of(channel))
                .fileData(screenshot.getImageData())
                .filename(generateFilename(screenshot))
                .title("Screenshot: " + screenshot.getName())
                .initialComment(":ghost: Screenshot from " + screenshot.getName() + " at " + 
                    screenshot.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

            FilesUploadResponse uploadResponse = slack.methods(slackToken).filesUpload(uploadRequest);
            
            if (uploadResponse.isOk()) {
                log.info("Screenshot uploaded to Slack successfully");
            } else {
                log.error("Failed to upload screenshot to Slack: {}", uploadResponse.getError());
            }
            
        } catch (IOException | SlackApiException e) {
            log.error("Error sending screenshot to Slack", e);
            throw new RuntimeException("Failed to send screenshot to Slack", e);
        }
    }

    /**
     * Send a simple message to Slack
     */
    public void sendMessageToSlack(String message, String slackToken, String channel) {
        try {
            log.info("Sending message to Slack channel: {}", channel);
            
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .token(slackToken)
                .channel(channel)
                .text(":ghost: " + message)
                .build();

            ChatPostMessageResponse response = slack.methods(slackToken).chatPostMessage(request);
            
            if (response.isOk()) {
                log.info("Message sent to Slack successfully");
            } else {
                log.error("Failed to send message to Slack: {}", response.getError());
            }
            
        } catch (IOException | SlackApiException e) {
            log.error("Error sending message to Slack", e);
            throw new RuntimeException("Failed to send message to Slack", e);
        }
    }

    /**
     * Scheduled task that runs every minute to check for due Slack tasks
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processSlackTasks() {
        log.debug("Checking for due Slack tasks...");
        
        List<SlackTask> dueTasks = slackTaskRepository.findDueTasks(LocalDateTime.now());
        
        for (SlackTask task : dueTasks) {
            try {
                log.info("Processing Slack task for site: {}", task.getSite().getName());
                
                // Get the latest screenshot for the site
                Screenshot screenshot = screenshotService.getLatestScreenshot(task.getSite().getName());
                
                // Send to Slack
                sendScreenshotToSlack(screenshot, task.getSlackToken(), task.getSlackChannel());
                
                // Update next scheduled time
                task.setScheduledTime(task.getScheduledTime().plus(task.getTaskInterval()));
                slackTaskRepository.save(task);
                
                log.info("Slack task completed for site: {}", task.getSite().getName());
                
            } catch (Exception e) {
                log.error("Error processing Slack task for site: {}", task.getSite().getName(), e);
            }
        }
    }
    
    private String generateFilename(Screenshot screenshot) {
        return screenshot.getName() + "_" + 
               screenshot.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + 
               "." + screenshot.getType();
    }
} 