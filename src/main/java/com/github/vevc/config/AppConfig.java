package com.github.vevc.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vevc
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String domain;
    private String port;
    private String uuid;
    private String xrayVersion;
    private String hy2Version;
    private String argoVersion;
    private String sbVersion;
    private String argoDomain;
    private String argoToken;
    private String realityPublicKey;
    private String realityPrivateKey;
    private String realityShortId;
    private String remarksPrefix;

    private Integer hy2Port;
    private Integer tuicPort;
    private Integer anyTlsPort;
    private String argoIp;

    private boolean enableSshx;
    private String gistId;
    private String ghToken;
    private String gistSshxFile;
    private String gistSubFile;
    private String telegramBotToken;
    private String telegramChatId;
    private String projectUrl;
    private boolean autoKeepAlive;
    private String warpMode;

    @PostConstruct
    public void init() {
        Map<String, String> fileParams = loadParamsFromFile();
        
        this.remarksPrefix = getParam(fileParams, "NAME", "paper-name", remarksPrefix, "vevc");
        this.domain = getParam(fileParams, "DOMAIN", "paper-domain", domain, "vevc.github.com");
        this.port = getParam(fileParams, "PORT", "paper-vless-port", port, "10008");
        this.port = getParam(fileParams, null, "paper-reality-port", this.port, this.port);

        String hy2PortStr = getParam(fileParams, "HY2_PORT", "paper-hy2-port", null, null);
        this.hy2Port = hy2PortStr != null ? Integer.parseInt(hy2PortStr) : Integer.parseInt(this.port) + 1;
        
        String tuicPortStr = getParam(fileParams, "TUIC_PORT", "paper-tuic-port", null, null);
        this.tuicPort = tuicPortStr != null ? Integer.parseInt(tuicPortStr) : Integer.parseInt(this.port) + 2;
        
        String anyTlsPortStr = getParam(fileParams, "ANYTLS_PORT", "paper-anytls-port", null, null);
        this.anyTlsPort = anyTlsPortStr != null ? Integer.parseInt(anyTlsPortStr) : Integer.parseInt(this.port) + 3;
        
        this.argoIp = getParam(fileParams, "CFIP", "paper-argo-ip", argoIp, "104.17.100.191");

        this.uuid = getParam(fileParams, "UUID", "paper-uuid", uuid, UUID.randomUUID().toString());
        this.sbVersion = getParam(fileParams, "SB_VERSION", "paper-sb-version", sbVersion, "1.11.0");
        this.argoVersion = getParam(fileParams, "ARGO_VERSION", "paper-argo-version", argoVersion, "2025.10.0");
        this.argoDomain = getParam(fileParams, "ARGO_DOMAIN", "paper-argo-domain", argoDomain, "xxx.trycloudflare.com");
        this.argoToken = getParam(fileParams, "ARGO_TOKEN", "paper-argo-token", argoToken, null);
        
        this.gistId = getParam(fileParams, "GIST_ID", "gist-id", gistId, null);
        this.ghToken = getParam(fileParams, "GH_TOKEN", "gh-token", ghToken, null);
        this.gistSshxFile = getParam(fileParams, "GIST_SSHX_FILE", "gist-sshx-file", gistSshxFile, "sshx.txt");
        this.gistSubFile = getParam(fileParams, "GIST_SUB_FILE", "gist-sub-file", gistSubFile, "sub.txt");
        
        this.telegramBotToken = getParam(fileParams, "BOT_TOKEN", "telegram-bot-token", telegramBotToken, null);
        this.telegramChatId = getParam(fileParams, "CHAT_ID", "telegram-chat-id", telegramChatId, null);
        
        this.projectUrl = getParam(fileParams, "PROJECT_URL", "project-url", projectUrl, null);
        
        String keepAliveStr = getParam(fileParams, "AUTO_ACCESS", "auto-access", null, null);
        if (keepAliveStr == null) {
            keepAliveStr = getParam(fileParams, null, "auto-keepalive", String.valueOf(autoKeepAlive), "false");
        }
        this.autoKeepAlive = Boolean.parseBoolean(keepAliveStr != null ? keepAliveStr : "true");

        this.enableSshx = Boolean.parseBoolean(getParam(fileParams, "ENABLE_SSHX", "paper-sshx", String.valueOf(enableSshx), "true"));
        this.warpMode = getParam(fileParams, "WARP_MODE", "warp-mode", warpMode, "auto");
        
        if (StringUtils.isBlank(this.projectUrl) && StringUtils.isNotBlank(this.domain)) {
            this.projectUrl = this.domain.startsWith("http") ? this.domain : "http://" + this.domain;
        }

        this.xrayVersion = StringUtils.defaultIfBlank(xrayVersion, "25.10.15");
        this.hy2Version = StringUtils.defaultIfBlank(hy2Version, "2.6.5");
    }

    private String getParam(Map<String, String> fileParams, String envKey, String propKey, String currentVal, String defaultVal) {
        if (envKey != null && System.getenv(envKey) != null) return System.getenv(envKey);
        if (fileParams.containsKey(propKey)) return fileParams.get(propKey);
        String underscoreKey = propKey.replace("-", "_");
        if (fileParams.containsKey(underscoreKey)) return fileParams.get(underscoreKey);
        return StringUtils.defaultIfBlank(currentVal, defaultVal);
    }

    private Map<String, String> loadParamsFromFile() {
        Map<String, String> params = new HashMap<>();
        Map<String, String> installParams = new HashMap<>();
        File propFile = new File(System.getProperty("user.dir"), "application.properties");
        if (!propFile.exists()) return params;

        try (BufferedReader reader = new BufferedReader(new FileReader(propFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                if (trimmed.startsWith("install=")) {
                    parseInstallLine(trimmed.substring(8), installParams);
                } else {
                    int idx = trimmed.indexOf("=");
                    if (idx > 0) {
                        params.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
                    }
                }
            }
            params.putAll(installParams);
        } catch (Exception e) {
        }
        return params;
    }

    private void parseInstallLine(String line, Map<String, String> params) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_-]+)=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            params.put(matcher.group(1), matcher.group(2));
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
