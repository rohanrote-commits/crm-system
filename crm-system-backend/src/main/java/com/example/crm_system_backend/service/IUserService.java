package com.example.crm_system_backend.service;

import com.example.crm_system_backend.dto.SignUpRequest;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface IUserService {


    public User registerUser(User user);
    public void updateUser();
    public void deleteUser();
  //  public<T> Optional<User> getUser(T t);

    //void getUser();

    Optional<User> getUser(UserDTO dto);
    boolean checkUserByEmail(String email);

   // Optional<User> getUserByMobileNumber(String number);

    boolean checkUserByMobileNumber(String number);

    public void getAllUser();

}
