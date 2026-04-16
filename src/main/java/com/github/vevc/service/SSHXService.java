package com.github.vevc.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vevc
 */
@Slf4j
@Service
public class SSHXService {

    @Getter
    private String sshxUrl;

    private static final Pattern URL_PATTERN = Pattern.compile("https://sshx\\.io/s/[a-zA-Z0-9]+#[a-zA-Z0-9]+");

    @Async
    public void start() {
        try {
            log.info("Starting SSHX...");
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", "curl -sSf https://sshx.io/get | sh -s run");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = URL_PATTERN.matcher(line);
                    if (matcher.find()) {
                        this.sshxUrl = matcher.group();
                        log.info("SSHX URL detected: {}", sshxUrl);
                    }
                    // Optional: log.debug(line);
                }
            }
            int exitCode = process.waitFor();
            log.info("SSHX process exited with code: {}", exitCode);
        } catch (Exception e) {
            log.error("Failed to start SSHX", e);
        }
    }
}
