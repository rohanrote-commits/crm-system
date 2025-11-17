package com.example.crm_system_backend.beans;

import com.example.crm_system_backend.entity.Lead;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeadList {
    private List<Lead> validLeadList = new ArrayList<>();
    private List<Lead> invalidLeadList = new ArrayList<>();
}
