package com.jercel.tech.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jercel.tech.helpers.GCSFolderUploader;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JercelUploadService {
    public static String bucketName="jercel-git-project-upload";

    @Autowired
    GCSFolderUploader gcsFolderUploader;

    @Autowired
    RedisPushService redisPushService;

    public ResponseEntity<String> uploadGitRepo(String gitRepoURL) {
        log.info("Inside uploadGitRepo : {}", gitRepoURL);
        try {
            Long startTime = System.currentTimeMillis();
            File file = new File("/Users/jaypalchauhan/Documents/Projects/Learn Spring Boot/vercel-clone/"
                    + gitRepoURL.split("/")[gitRepoURL.split("/").length - 1]);
            Git.cloneRepository()
                    .setURI(gitRepoURL)
                    .setDirectory(file)
                    .call();
                    log.info("Time taken by gitCloneRepo : {}", System.currentTimeMillis()-startTime);

            String folderId = readFile(file);
            removeFile(file);
            if(folderId!=null && !folderId.isEmpty())
                return ResponseEntity.ok().body(folderId);
        } catch (Exception e) {
            log.error("Exception in uploadGitRepo", e);
        }
        return ResponseEntity.internalServerError().body("Failure while upload please try again after sometime");
    }

    private void removeFile(File file) throws IOException {
        Long startTime = System.currentTimeMillis();

        Path startPath = Paths.get(file.getAbsolutePath());

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // log.info("File: " + file.toString());
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ioc) throws IOException {
                    // log.info("Directory: " + dir.toString());
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // log.error("Failed to access file: " + file.toString() + " due to " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Time taken by removeFile : {}", System.currentTimeMillis()-startTime);

    }

    private String readFile(File file) {
        log.info("Inside readFile");
        try {
            return gcsFolderUploader.uploadFolder(file.toPath());
        } catch (Exception e) {
            log.error("Exception in readFile ", e);
        }
        return null;
    }

    public ResponseEntity<String> status(String id) {
        String value = redisPushService.getValue(id);
        if(value!=null){
            return ResponseEntity.ok().body("uploading");
        }
        return ResponseEntity.ok().body(value);
    }

}
