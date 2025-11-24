package com.example.crm_system_backend.constants;

import lombok.Getter;

@Getter
public enum Roles {
//    MASTER_ADMIN,
//    ADMIN,
//    USER
    MASTER_ADMIN("Master Admin"),
    ADMIN("Admin"),
    BASIC("Basic"),
    USER("Basic");

    private final String description;

    Roles(String description) {
        this.description = description;
    }


}
