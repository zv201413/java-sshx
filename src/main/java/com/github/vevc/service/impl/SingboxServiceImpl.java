package com.github.vevc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.vevc.config.AppConfig;
import com.github.vevc.service.AbstractAppService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author vevc
 */
@Slf4j
@Service
public class SingboxServiceImpl extends AbstractAppService {

    private static final String APP_NAME = "sb";
    private static final String APP_CONFIG_NAME = "config.json";
    private static final String APP_ARCHIVE_NAME = "sing-box.tar.gz";
    private static final String APP_DOWNLOAD_URL = "https://github.com/SagerNet/sing-box/releases/download/v%s/sing-box-%s-linux-%s.tar.gz";
    
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public SingboxServiceImpl(AppConfig appConfig) {
        super(appConfig);
    }

    @Override
    protected String getAppDownloadUrl() {
        String arch = OS_IS_ARM ? "arm64" : "amd64";
        return String.format(APP_DOWNLOAD_URL, appConfig.getSbVersion(), appConfig.getSbVersion(), arch);
    }

    @Override
    public void install() throws Exception {
        if (new File(this.getBinaryPath(), APP_NAME).exists()) {
            log.info("sing-box already exists, skip install");
            return;
        }

        File binaryPath = this.initBinaryPath();
        File targetFile = new File(binaryPath, APP_ARCHIVE_NAME);
        this.download(this.getAppDownloadUrl(), targetFile);
        log.info("sing-box archive downloaded successfully");
        
        // Extract tar.gz
        executeAndCapture("tar", "-xzf", targetFile.getAbsolutePath(), "-C", binaryPath.getAbsolutePath(), "--strip-components=1");
        log.info("sing-box archive extracted successfully");
        
        FileUtils.delete(targetFile);
        File destFile = new File(binaryPath, APP_NAME);
        // sing-box tar contains a 'sing-box' binary
        File extractedBin = new File(binaryPath, "sing-box");
        if (extractedBin.exists()) {
            FileUtils.moveFile(extractedBin, destFile);
        }
        
        this.setExecutePermission(destFile.toPath());
        log.info("sing-box installed successfully");

        this.generateConfig(binaryPath);
        log.info("sing-box config generated successfully");

        this.updateSpringConfig();
        log.info("Spring application.yml config updated successfully");
    }

    private void generateConfig(File binaryPath) throws Exception {
        ObjectNode root = jsonMapper.createObjectNode();
        
        // Log
        ObjectNode logNode = root.putObject("log");
        logNode.put("level", "error");
        logNode.put("timestamp", true);

        // Inbounds
        ArrayNode inbounds = root.putArray("inbounds");

        // 1. Argo Inbound (VLESS-WS)
        ObjectNode argoIn = inbounds.addObject();
        argoIn.put("type", "vless");
        argoIn.put("tag", "vless-ws-in");
        argoIn.put("listen", "::");
        argoIn.put("listen_port", 8001);
        ArrayNode argoUsers = argoIn.putArray("users");
        argoUsers.addObject().put("uuid", appConfig.getUuid());
        ObjectNode argoTrans = argoIn.putObject("transport");
        argoTrans.put("type", "ws");
        argoTrans.put("path", "/vless-argo");

        // 2. Reality Inbound
        this.generateRealityKeys(new File(binaryPath, APP_NAME));
        ObjectNode realityIn = inbounds.addObject();
        realityIn.put("type", "vless");
        realityIn.put("tag", "vless-reality-in");
        realityIn.put("listen", "::");
        realityIn.put("listen_port", Integer.parseInt(appConfig.getPort()));
        ArrayNode realityUsers = realityIn.putArray("users");
        ObjectNode user = realityUsers.addObject();
        user.put("uuid", appConfig.getUuid());
        user.put("flow", "xtls-rprx-vision");
        ObjectNode tls = realityIn.putObject("tls");
        tls.put("enabled", true);
        tls.put("server_name", "www.iij.ad.jp");
        ObjectNode reality = tls.putObject("reality");
        reality.put("enabled", true);
        ObjectNode handshake = reality.putObject("handshake");
        handshake.put("server", "www.iij.ad.jp");
        handshake.put("server_port", 443);
        reality.put("private_key", appConfig.getRealityPrivateKey());
        reality.putArray("short_id").add("");

        // 3. Hysteria2 Inbound
        ObjectNode hy2In = inbounds.addObject();
        hy2In.put("type", "hysteria2");
        hy2In.put("tag", "hy2-in");
        hy2In.put("listen", "::");
        hy2In.put("listen_port", Integer.parseInt(appConfig.getPort()) + 1); // 默认+1或自定义
        ArrayNode hy2Users = hy2In.putArray("users");
        hy2Users.addObject().put("password", appConfig.getUuid());
        ObjectNode hy2Tls = hy2In.putObject("tls");
        hy2Tls.put("enabled", true);
        hy2Tls.putArray("alpn").add("h3");
        // Reuse certs from util if possible or generic path
        hy2Tls.put("certificate_path", new File(binaryPath, "cert.pem").getAbsolutePath());
        hy2Tls.put("key_path", new File(binaryPath, "private.key").getAbsolutePath());

        // Outbounds
        ArrayNode outbounds = root.putArray("outbounds");
        outbounds.addObject().put("type", "direct").put("tag", "direct");
        outbounds.addObject().put("type", "dns").put("tag", "dns-out");

        // Route
        ObjectNode route = root.putObject("route");
        ArrayNode rules = route.putArray("rules");
        rules.addObject().put("protocol", "dns").put("outbound", "dns-out");
        route.put("final", "direct");

        File configFile = new File(binaryPath, APP_CONFIG_NAME);
        Files.writeString(configFile.toPath(), jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void generateRealityKeys(File binaryFile) throws Exception {
        String output = executeAndCapture(binaryFile.getAbsolutePath(), "generate", "reality-keypair");
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("PrivateKey: ")) {
                appConfig.setRealityPrivateKey(line.replace("PrivateKey: ", "").trim());
            } else if (line.startsWith("PublicKey: ")) {
                appConfig.setRealityPublicKey(line.replace("PublicKey: ", "").trim());
            }
        }
        Assert.hasText(appConfig.getRealityPrivateKey(), "Failed to generate reality keys");
    }

    @Async
    @Override
    public void startup() throws Exception {
        File binaryPath = this.getBinaryPath();
        File appFile = new File(binaryPath, APP_NAME);
        File configFile = new File(binaryPath, APP_CONFIG_NAME);
        
        while (true) {
            ProcessBuilder pb = new ProcessBuilder(appFile.getAbsolutePath(), "run", "-c", configFile.getAbsolutePath());
            pb.directory(binaryPath);
            // Redirect to dev null to avoid log bloat, or handle properly
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            
            log.info("Starting sing-box...");
            int exitCode = this.startProcess(pb);
            log.info("sing-box process exited with code: {}, restarting in 3s...", exitCode);
            TimeUnit.SECONDS.sleep(3);
        }
    }

    @Override
    protected String getAppName() {
        return APP_NAME;
    }
}
