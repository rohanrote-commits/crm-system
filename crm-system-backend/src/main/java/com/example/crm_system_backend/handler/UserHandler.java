package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.UserSessionRepo;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class UserHandler implements IHandler<UserDTO> {

    @Autowired
    private IUserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private UserSessionRepo userSessionRepo;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDTO save(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if(user.getAddress() == null){
            if(user.getCity() != null ||
                    user.getState() != null ||
                    user.getCountry() != null ||
                    user.getPinCode() != null) {
                throw new UserException(ErrorCode.INVALID_ADDRESS);
            }
        }


        BeanUtils.copyProperties(userService.registerUser(user),userDTO);
        return userDTO;
    }

    @Override
    public List<UserDTO> getAll() {
        return List.of();
    }

    public List<UserDTO> getUsers(Long id) {
        log.info("Request for getting users is in user Handler for user id" + id);
        List<User> users;
        if(userRepo.findRoleById(id) == Roles.MASTER_ADMIN){
            log.info("Request for getting users is in user Handler for master admin");
            users = userService.getAllUserByMasterAdmin(id);
        }else {
            log.info("Request for getting users is in user Handler for admin");
            users = userService.getAllUsersByAdmin(id);}
            return users.stream().map(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setEmailOfAdminRegistered(userRepo.findEmailById(user.getRegisteredBy()));
                BeanUtils.copyProperties(user, userDTO);
                return userDTO;
            }).toList();

    }


    @Override
    public UserDTO edit(Long Id, UserDTO entity) {
        return null;
    }

    @Override
    public void delete(Long leadId) {

    }

    @Override
    public void bulkUpload() {

    }
}


