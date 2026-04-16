package com.github.vevc.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

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
        domain = StringUtils.defaultIfBlank(domain, "vevc.github.com");
        port = StringUtils.defaultIfBlank(port, "10008");
        uuid = StringUtils.defaultIfBlank(uuid, UUID.randomUUID().toString());
        xrayVersion = StringUtils.defaultIfBlank(xrayVersion, "25.10.15");
        hy2Version = StringUtils.defaultIfBlank(hy2Version, "2.6.5");
        argoVersion = StringUtils.defaultIfBlank(argoVersion, "2025.10.0");
        sbVersion = StringUtils.defaultIfBlank(sbVersion, "1.11.0");
        argoDomain = StringUtils.defaultIfBlank(argoDomain, "xxx.trycloudflare.com");
        remarksPrefix = StringUtils.defaultIfBlank(remarksPrefix, "vevc");
        
        gistSshxFile = StringUtils.defaultIfBlank(gistSshxFile, "sshx.txt");
        gistSubFile = StringUtils.defaultIfBlank(gistSubFile, "sub.txt");
        warpMode = StringUtils.defaultIfBlank(warpMode, "auto");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
