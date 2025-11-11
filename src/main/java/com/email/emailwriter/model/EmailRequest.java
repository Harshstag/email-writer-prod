package com.email.emailwriter.model;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;
    private String suggestions;
    private String length;
}
