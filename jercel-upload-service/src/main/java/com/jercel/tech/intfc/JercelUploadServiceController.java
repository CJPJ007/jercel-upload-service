package com.jercel.tech.intfc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface JercelUploadServiceController {

    @PostMapping("/uploadGitRepo")
    public ResponseEntity<String> uploadGitRepo(@RequestBody String gitRepoURL);
    
    @GetMapping("/status/{id}")
    public ResponseEntity<String> status(@PathVariable String id);

}
