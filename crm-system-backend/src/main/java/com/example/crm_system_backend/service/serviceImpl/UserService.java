package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepo userRepo;


    @Override
    public User registerUser(User user) {
        return userRepo.save(user);
    }

    @Override
    public void updateUser() {

    }

    @Override
    public void deleteUser() {

    }

    @Override
    public Optional<User> getUser(UserDTO dto) {

       return userRepo.findByEmailAndPassword(dto.getEmail(),dto.getPassword());
    }


    public boolean checkUserByMobileNumber(String number){
        return userRepo.findByMobileNumber(number);
    }

    @Override
    public void getAllUser() {

    }
}
