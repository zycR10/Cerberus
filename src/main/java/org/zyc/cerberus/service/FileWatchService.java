package org.zyc.cerberus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Zuo Yichen
 * @Date: 2020/4/13 23:42
 */
@Service
public class FileWatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchService.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final AtomicInteger imgIndex = new AtomicInteger(0);
    private boolean stop = false;

    @Value("${file.watch.path}")
    String watchPath;

    @Value("${file.copy.path}")
    String copyPath;

    public void watch() throws IOException, InterruptedException {
        WatchService watchService
                = FileSystems.getDefault().newWatchService();

        Path path = Paths.get(watchPath);
        path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );
        WatchKey key;
        while (!stop && (key = watchService.take()) != null) {
            try {
                for (WatchEvent<?> event : key.pollEvents()) {
                    LOGGER.info("Event kind: {}" + ". File affected: {}", event.kind(), event.context());
                    if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                        executorService.execute(() -> {
//                            try {
//                                Thread.sleep(600);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            File oldFile = new File(watchPath + event.context());
                            File newFile = new File(copyPath + event.context() + imgIndex.getAndIncrement());
                            try {
                                Files.copy(oldFile.toPath(), newFile.toPath());
                                LOGGER.info("copy file success");
                            } catch (IOException e) {
                                LOGGER.error("copy file has exception ", e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                LOGGER.error("watch event has exception", e);
            } finally {
                key.reset();
            }
        }
    }
}
