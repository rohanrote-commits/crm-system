package com.example.crm_system_backend.service;

import com.example.crm_system_backend.dto.UserDTO;

public interface IUserService {

    public void registerUser(UserDTO userDTO);
    public void updateUser();
    public void deleteUser();
    public void getUser();
    public void getAllUser();

}
