package com.email.emailwriter.controller;

import com.email.emailwriter.model.EmailRequest;
import com.email.emailwriter.servie.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest) {
        String response = emailService.generateEmailReply(emailRequest);
        log.info(":::::::::::::::::::::: Generated Email Response Controller calling: {}", response);
        return ResponseEntity.ok(response);
    }


}