package com.github.vevc.service;

import com.github.vevc.service.impl.ArgoServiceImpl;
import com.github.vevc.service.impl.SingboxServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author vevc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppService {

    private final ArgoServiceImpl argoService;
    private final SingboxServiceImpl singboxService;

    public void install() {
        try {
            argoService.install();
            singboxService.install();
        } catch (Exception e) {
            log.error("App install failed", e);
            System.exit(1);
        }
    }

    public void startup() {
        try {
            argoService.startup();
            singboxService.startup();
        } catch (Exception e) {
            log.error("App startup failed", e);
        }
    }
}
