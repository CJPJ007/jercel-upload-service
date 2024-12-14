package com.jercel.tech.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jercel.tech.intfc.JercelUploadServiceController;
import com.jercel.tech.service.JercelUploadService;
import com.jercel.tech.validators.JercelUrlValidator;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/")
@Slf4j
public class JercelUploadServiceControllerImpl implements JercelUploadServiceController{

    @Value("${git.url.validation.regex}")
    private String gitUrlValReg;

    @Autowired
    JercelUploadService jercelUploadService;

    @Override
    public ResponseEntity<String> uploadGitRepo(String gitRepoURL) {
        log.info("Inside uploadGitRepo : {}",gitRepoURL);
        if(!JercelUrlValidator.validateURL(gitRepoURL, gitUrlValReg)){
            return ResponseEntity.badRequest().body("Invalid url or not a github url");
        }

        return jercelUploadService.uploadGitRepo(gitRepoURL);
    }

    @Override
    public ResponseEntity<String> status(String id) {
        return jercelUploadService.status(id);
    }
    
}
