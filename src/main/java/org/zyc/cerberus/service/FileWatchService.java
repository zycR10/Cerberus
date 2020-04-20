package org.zyc.cerberus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Zuo Yichen
 * @Date: 2020/4/13 23:42
 */
@Service
public class FileWatchService implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchService.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final AtomicInteger imgIndex = new AtomicInteger(0);
    AtomicBoolean stop = new AtomicBoolean();

    @Value("${file.watch.path}")
    String watchPath;

    @Value("${file.copy.path}")
    String copyPath;

    public void watch(List<String> oriPath, List<String> destPath) throws IOException, InterruptedException {
        WatchService watchService
                = FileSystems.getDefault().newWatchService();
        String watchedFilePath = getFilePath(oriPath, false);
        String targetFilePath = getFilePath(destPath, true);
        boolean needCopy = !StringUtils.isEmpty(targetFilePath);
        // check path for update event happen one file in a loop
        if (needCopy &&
                (watchedFilePath.equals(targetFilePath) || targetFilePath.startsWith(watchedFilePath))) {
            LOGGER.error("watch file path & target file path is same, please check your config, this may cause " +
                    "endless loop!!!");
            throw new InterruptedException("same file path is unacceptable!!");
        }
        Path path = Paths.get(watchedFilePath);
        path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );
        WatchKey key;
        while ((key = watchService.take()) != null && !stop.get()) {
            try {
                for (WatchEvent<?> event : key.pollEvents()) {
                    LOGGER.info("Event kind: {}" + ". File affected: {}", event.kind(), event.context());
                    if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind()) && needCopy) {
                        executorService.execute(() -> {
                            File oldFile = new File(watchedFilePath + event.context());
                            File newFile = new File(targetFilePath + System.currentTimeMillis() + event.context());
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

    private String getFilePath(List<String> paths, boolean isNullable) {
        if (CollectionUtils.isEmpty(paths)) {
            if (isNullable) {
                return null;
            } else {
                return watchPath;
            }
        }
        String path = paths.get(0);
        if (path.endsWith("\\") || path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        watch(args.getOptionValues("file.watch.path"),
                args.getOptionValues("file.copy.path"));
    }

    public void shutdown() {
        stop.set(true);
    }

    public void start() {
        stop.getAndSet(false);
    }
}
