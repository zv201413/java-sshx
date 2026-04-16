package com.github.vevc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.vevc.config.AppConfig;
import com.github.vevc.config.ApplicationYamlVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * @author vevc
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAppService {

    private static final File SPRING_CONFIG_PATH = new File(System.getProperty("user.dir"), "application.yml");
    private static final File BINARY_PATH = new File(System.getProperty("user.dir"), "bin");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    protected static final boolean OS_IS_ARM;

    protected final AppConfig appConfig;

    static {
        String arch = System.getProperty("os.arch").toLowerCase();
        OS_IS_ARM = arch.contains("arm") || arch.contains("aarch64");
    }

    /**
     * get app download url
     *
     * @return url
     */
    protected abstract String getAppDownloadUrl();

    /**
     * install app
     *
     * @throws Exception e
     */
    protected abstract void install() throws Exception;

    /**
     * start app
     *
     * @throws Exception e
     */
    protected abstract void startup() throws Exception;

    /**
     * get app name
     *
     * @return appName
     */
    protected abstract String getAppName();

    protected File initBinaryPath() throws IOException {
        File binaryPath = new File(BINARY_PATH, this.getAppName());
        FileUtils.forceMkdir(binaryPath);
        FileUtils.cleanDirectory(binaryPath);
        return binaryPath;
    }

    protected File getBinaryPath() {
        return new File(BINARY_PATH, this.getAppName());
    }

    protected void setExecutePermission(Path destFile) throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(destFile);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(destFile, perms);
    }

    protected void download(String downloadUrl, File file) throws IOException {
        try (InputStream in = new URL(downloadUrl).openStream()) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    protected void updateSpringConfig() throws Exception {
        ApplicationYamlVo applicationYamlVo = new ApplicationYamlVo();
        applicationYamlVo.setAppConfig(appConfig);
        OBJECT_MAPPER.writeValue(SPRING_CONFIG_PATH, applicationYamlVo);
    }

    /**
     * start process
     *
     * @param pb processBuilder
     * @return exitCode
     * @throws Exception e
     */
    protected int startProcess(ProcessBuilder pb) throws Exception {
        Process process = pb.start();
        try (
                InputStream in = process.getInputStream();
                InputStreamReader inReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inReader)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
        return process.waitFor();
    }

    protected String executeAndCapture(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        process.waitFor();
        return sb.toString();
    }
}
