package com.example.crm_system_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;

public class EmailRequest {
    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String subject;

    private String body;

    @NotNull
    private ZoneId timeZone;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }
}