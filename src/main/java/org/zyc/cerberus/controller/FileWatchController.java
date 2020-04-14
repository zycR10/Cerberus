package org.zyc.cerberus.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zyc.cerberus.service.FileWatchService;

import java.nio.file.*;

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
    public void fileWacth() {
        try {
            fileWatchService.watch();
        } catch (Exception e) {
            LOGGER.error("watch file has ex : {}", e.getMessage());
        }
    }
}
