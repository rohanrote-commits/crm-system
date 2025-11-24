package com.example.crm_system_backend.beans;

import com.example.crm_system_backend.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserList {
    private List<User> validUserList = new ArrayList<>();
    private List<User> invalidUserList = new ArrayList<>();
}
