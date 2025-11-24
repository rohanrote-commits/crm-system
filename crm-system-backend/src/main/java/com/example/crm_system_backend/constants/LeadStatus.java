package com.example.crm_system_backend.constants;

import lombok.Getter;

@Getter
public enum LeadStatus {
    ADDED(0),
    CONTACTED(1),
    CONVERTED(2),
    NOT_CONVERTED(3);

    private int value;

    LeadStatus(int value) {
        this.value = value;
    }

}
