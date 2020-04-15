package org.zyc.cerberus.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zyc.cerberus.service.FileWatchService;

/**
 * @Author: Zuo Yichen
 * @Date: 2020/4/13 23:38
 */
@RestController
@RequestMapping("/file")
public class FileWatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchController.class);

    @Autowired
    FileWatchService fileWatchService;

    @GetMapping("/watch/start")
    public void start() {
        try {
            fileWatchService.start();
        } catch (Exception e) {
            LOGGER.error("start file has ex : ", e);
        }
    }

    @GetMapping("/watch/shutdown")
    public void shutdown() {
        try {
            fileWatchService.shutdown();
        } catch (Exception e) {
            LOGGER.error("shutdown  has ex : ", e);
        }
    }
}
