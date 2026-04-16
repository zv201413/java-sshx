package com.github.vevc.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author vevc
 */
@Data
public class ApplicationYamlVo {

    private SpringConfig spring = new SpringConfig();
    private AppConfigVo app = new AppConfigVo();

    public void setAppConfig(AppConfig appConfig) {
        this.getApp().setDomain(appConfig.getDomain());
        this.getApp().setPort(appConfig.getPort());
        this.getApp().setUuid(appConfig.getUuid());
        this.getApp().setXrayVersion(appConfig.getXrayVersion());
        this.getApp().setHy2Version(appConfig.getHy2Version());
        this.getApp().setArgoVersion(appConfig.getArgoVersion());
        this.getApp().setSbVersion(appConfig.getSbVersion());
        this.getApp().setArgoDomain(appConfig.getArgoDomain());
        this.getApp().setArgoToken(appConfig.getArgoToken());
        this.getApp().setRealityPublicKey(appConfig.getRealityPublicKey());
        this.getApp().setRealityPrivateKey(appConfig.getRealityPrivateKey());
        this.getApp().setRealityShortId(appConfig.getRealityShortId());
        this.getApp().setRemarksPrefix(appConfig.getRemarksPrefix());
        this.getApp().setEnableSshx(appConfig.isEnableSshx());
        this.getApp().setGistId(appConfig.getGistId());
        this.getApp().setGhToken(appConfig.getGhToken());
        this.getApp().setGistSshxFile(appConfig.getGistSshxFile());
        this.getApp().setGistSubFile(appConfig.getGistSubFile());
        this.getApp().setTelegramBotToken(appConfig.getTelegramBotToken());
        this.getApp().setTelegramChatId(appConfig.getTelegramChatId());
        this.getApp().setProjectUrl(appConfig.getProjectUrl());
        this.getApp().setAutoKeepAlive(appConfig.isAutoKeepAlive());
        this.getApp().setWarpMode(appConfig.getWarpMode());
    }

    @Data
    public static class AppConfigVo {
        private String domain;
        private String port;
        private String uuid;
        @JsonProperty("xray-version")
        private String xrayVersion;
        @JsonProperty("hy2-version")
        private String hy2Version;
        @JsonProperty("argo-version")
        private String argoVersion;
        @JsonProperty("sb-version")
        private String sbVersion;
        @JsonProperty("argo-domain")
        private String argoDomain;
        @JsonProperty("argo-token")
        private String argoToken;
        @JsonProperty("reality-public-key")
        private String realityPublicKey;
        @JsonProperty("reality-private-key")
        private String realityPrivateKey;
        @JsonProperty("reality-short-id")
        private String realityShortId;
        @JsonProperty("remarks-prefix")
        private String remarksPrefix;
        @JsonProperty("enable-sshx")
        private boolean enableSshx;
        @JsonProperty("gist-id")
        private String gistId;
        @JsonProperty("gh-token")
        private String ghToken;
        @JsonProperty("gist-sshx-file")
        private String gistSshxFile;
        @JsonProperty("gist-sub-file")
        private String gistSubFile;
        @JsonProperty("telegram-bot-token")
        private String telegramBotToken;
        @JsonProperty("telegram-chat-id")
        private String telegramChatId;
        @JsonProperty("project-url")
        private String projectUrl;
        @JsonProperty("auto-keepalive")
        private boolean autoKeepAlive;
        @JsonProperty("warp-mode")
        private String warpMode;
    }

    @Data
    public static class SpringConfig {
        private Application application = new Application();
    }

    @Data
    public static class Application {
        private String name = "java-xah";
    }
}
