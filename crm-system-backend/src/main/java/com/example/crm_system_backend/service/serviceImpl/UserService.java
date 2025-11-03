package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepo userRepo;


    @Override
    public User registerUser(User user) {
        user.setRegisteredOn(LocalDateTime.now());
        return userRepo.save(user);
    }

    @Override
    public void updateUser() {

    }

    @Override
    public void deleteUser() {

    }

    @Override
    public List<User> getAllUsers() {
        return List.of();
    }

    @Override
    public List<User> getAllUserByMasterAdmin(Long id) {
        log.info("Request for getting users is in user Handler for master admin");
        List<User> user = new ArrayList<>();
        List<User> users = userRepo.findUsersByRegisteredBy(id);
        user.addAll(users);
        log.info("users:{}",users);
        List<User> admins = users.stream()
                .filter(user1 -> user1.getRole() == Roles.ADMIN)
                .toList(); // Java 16+ List.copyOf style
        log.info("admins:{}",admins,"Registered by ", id);
        admins.forEach(user1 -> {
            List<User> users2 = getAllUsersByAdmin(user1.getId());
            user.addAll(users2);
            log.info("users2:{}",users2);
        });
        return user;
    }

    @Override
    public List<User> getAllUsersByAdmin(Long id) {
        return userRepo.findUsersByRegisteredBy(id);
    }


    @Override
    public Optional<User> getUser(UserDTO dto) {
       return userRepo.findByEmailAndPassword(dto.getEmail(),dto.getPassword());
    }

    @Override
    public boolean checkUserByEmail(String email) {

        return userRepo.existsByEmail(email);
    }


    @Override
    public boolean checkUserByMobileNumber(String number){

        return userRepo.existsByMobileNumber(number);
    }

    @Override
    public void getAllUser() {

    }
}
