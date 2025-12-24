package com.example.crm_system_backend.utils;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class GeneralUtils {
    public String maskUserId(Long userId) {
        return Base64.getEncoder()
                .encodeToString((userId + ":MY_SECRET").getBytes());
    }
    public Long unmaskUserId(String maskedId) {
        String decoded = new String(Base64.getDecoder().decode(maskedId));
        return Long.valueOf(decoded.split(":")[0]);
    }

}
