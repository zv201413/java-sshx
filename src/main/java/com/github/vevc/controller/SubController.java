package com.github.vevc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * @author vevc
 */
@RestController
@RequiredArgsConstructor
public class SubController {

    private static final Path NODE_FILE_PATH = Paths.get(System.getProperty("user.dir"), "node.txt");

    @GetMapping("/sub")
    public String getSubscription() throws IOException {
        if (!Files.exists(NODE_FILE_PATH)) {
            return "No nodes available yet.";
        }
        String content = Files.readString(NODE_FILE_PATH);
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    @GetMapping(value = "/list", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getList() throws IOException {
        if (!Files.exists(NODE_FILE_PATH)) {
            return "No nodes available yet.";
        }
        return Files.readString(NODE_FILE_PATH);
    }
}
