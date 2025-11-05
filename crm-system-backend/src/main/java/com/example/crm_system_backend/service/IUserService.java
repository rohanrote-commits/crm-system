package com.example.crm_system_backend.service;

import com.example.crm_system_backend.dto.SignUpRequest;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface IUserService {


    public User registerUser(User user);
    public void updateUser();
    public void deleteUser(User user);
  //  public<T> Optional<User> getUser(T t);

    //void getUser();
    List<User> getAllUsersByAdmin(Long id);
    List<User> getAllUsers();
    List<User> getAllUserByMasterAdmin(Long id);


    Optional<User> getUser(UserDTO dto);
    boolean checkUserByEmail(String email);

   // Optional<User> getUserByMobileNumber(String number);

    boolean checkUserByMobileNumber(String number);

    public void getAllUser();

}
