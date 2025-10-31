package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.SignUpRequest;
import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.UserSessionRepo;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserHandler implements IHandler<User> {

    @Autowired
    private IUserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private UserSessionRepo userSessionRepo;
    @Autowired
    private JwtUtil jwtUtil;
    

    @Override
    public User save(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if(user.getAddress() == null){
            if(user.getCity() != null ||
                    user.getState() != null ||
                    user.getCountry() != null ||
                    user.getPinCode() != null) {
                throw new UserException(ErrorCode.INVALID_ADDRESS)
            }
        }

        return userService.registerUser(user);
    }

    @Override
    public List<User> getAll() {
        return List.of();
    }


    @Override
    public User edit(Long Id, User entity) {
        return null;
    }

    @Override
    public void delete(Long leadId) {

    }

    @Override
    public void bulkUpload() {

    }
}
