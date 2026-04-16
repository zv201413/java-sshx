package com.github.vevc.service;

import com.github.vevc.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vevc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final AppConfig appConfig;
    private final RestTemplate restTemplate;

    @Async
    public void syncToGist(String fileName, String content) {
        if (appConfig.getGistId() == null || appConfig.getGhToken() == null) {
            log.warn("Gist ID or GH Token is missing, skipping sync");
            return;
        }

        try {
            String url = "https://api.github.com/gists/" + appConfig.getGistId();
            
            Map<String, Object> body = new HashMap<>();
            body.put("description", "java-xah 节点同步");
            
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("content", "最后更新时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n---\n" + content);
            
            Map<String, Object> files = new HashMap<>();
            files.put(fileName, fileContent);
            body.put("files", files);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "token " + appConfig.getGhToken());
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            log.info("✅ Gist sync successful for file: {}", fileName);
        } catch (Exception e) {
            log.error("❌ Gist sync failed", e);
        }
    }

    @Async
    public void sendTelegramNotification(String message) {
        if (appConfig.getTelegramBotToken() == null || appConfig.getTelegramChatId() == null) {
            log.warn("Telegram configuration missing, skipping notification");
            return;
        }

        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                    appConfig.getTelegramBotToken(),
                    appConfig.getTelegramChatId(),
                    message);
            restTemplate.getForObject(url, String.class);
            log.info("✅ Telegram notification sent");
        } catch (Exception e) {
            log.error("❌ Telegram notification failed", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void keepAliveTask() {
        if (!appConfig.isAutoKeepAlive() || appConfig.getProjectUrl() == null) {
            return;
        }

        try {
            log.info("Running keep-alive task for: {}", appConfig.getProjectUrl());
            // Using a simple GET request for keep-alive
            restTemplate.getForObject(appConfig.getProjectUrl(), String.class);
            
            // Also notify the keep-alive service mentioned in nodejs-sshx if needed
            Map<String, String> body = Map.of("url", appConfig.getProjectUrl());
            restTemplate.postForObject("https://keep.gvrander.eu.org/add-url", body, String.class);
            log.info("✅ Keep-alive successful");
        } catch (Exception e) {
            log.error("❌ Keep-alive failed", e);
        }
    }
}
