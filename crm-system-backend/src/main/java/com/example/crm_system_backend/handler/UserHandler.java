package com.example.crm_system_backend.handler;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.entity.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.exception.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.helper.UserExcelHelper;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.repository.UserSessionRepo;
import com.example.crm_system_backend.service.serviceImpl.UserService;
import com.example.crm_system_backend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Component
public class UserHandler implements IHandler<UserDTO> {

    @Autowired
    private IUserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthHandler authHandler;
    @Autowired
    private UserExcelHelper userExcelHelper;

    @Override
    public UserDTO save(UserDTO userDTO) {
        if(userService.checkUserByEmail(userDTO.getEmail())) throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
        if(userService.checkUserByMobileNumber(userDTO.getMobileNumber())) throw new UserException(ErrorCode.MOBILE_NUMBER_ALREADY_EXISTS);
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
        user.setRegisteredOn(java.time.LocalDateTime.now());
        BeanUtils.copyProperties(userService.registerUser(user),userDTO);
        return userDTO;
    }

    @Override
    public List<UserDTO> getAll() {
        return List.of();
    }
    public User getById(Long id){
       return userService.getUserById(id).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
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

    public UserDTO forgetPassword(UserDTO forgetPasswordDTO){
        Optional<User> userOptional = userRepo.getUserByEmail(forgetPasswordDTO.getEmail());
        if(userOptional.isPresent()){
            if(userOptional.get().getPassword().equals(forgetPasswordDTO.getPassword())){
                throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
            }
            userOptional.get().setPassword(forgetPasswordDTO.getPassword());
            userService.registerUser(userOptional.get());
            return forgetPasswordDTO;
        }else {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }
    }


    @Override
    public UserDTO edit(Long Id, UserDTO entity) {
        Optional<User> userOptional = userService.getUserById(Id);
        if (userOptional.isPresent()) {
            if (entity.getPassword() != null) {
                userOptional.get().setPassword(entity.getPassword());
            }
            if(entity.getMobileNumber() != null){
            userOptional.get().setMobileNumber(entity.getMobileNumber());}
        }
            if (entity.getAddress() != null) {
                userOptional.get().setAddress(entity.getAddress());
                userOptional.get().setCity(entity.getCity());
                userOptional.get().setState(entity.getState());
                userOptional.get().setCountry(entity.getCountry());
                userOptional.get().setPinCode(entity.getPinCode());
            }
            userService.registerUser(userOptional.get());
            return entity;


        }

    public User editSubUser(Long id, UserDTO userDTO){
        List<UserDTO> users = getUsers(id);
        UserDTO userDTO1 = users.stream().filter(user -> user.getEmail().equals(userDTO.getEmail()))
                .findFirst().orElseThrow(() -> new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL));
        Optional<User> user  = userService.getUser(userDTO1);
        boolean flag = false;
        if (user.isPresent()) {


            if (!userDTO.getMobileNumber().isEmpty() && !userDTO.getMobileNumber().equals(user.get().getMobileNumber())) {
                user.get().setMobileNumber(userDTO.getMobileNumber());
                flag = true;
            }
            log.info("Mobile Number is " +userDTO.getMobileNumber());


            if (!userDTO.getAddress().isEmpty() && !userDTO.getAddress().equals(user.get().getAddress())) {
                flag = true;
                user.get().setAddress(userDTO.getAddress());
                user.get().setCity(userDTO.getCity());
                user.get().setState(userDTO.getState());
                user.get().setCountry(userDTO.getCountry());
                user.get().setPinCode(userDTO.getPinCode());
            }
            if(!flag){
                throw new UserException(ErrorCode.USER_DATA_NOT_UPDATABLE);
            }
            return userService.registerUser(user.get());
        }else {
            throw new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL);
        }

    }



    @Override
    public void delete(Long leadId) {
        Optional<User> user = userService.getUserById(leadId);
        if(user.isPresent()){
            if(user.get().getRole() == Roles.MASTER_ADMIN){
                List<User> users = userService.getAllUserByMasterAdmin(leadId);
                users.stream().forEach(user1 -> {
                    userService.deleteUser(user1);
                });
            }else if(user.get().getRole() == Roles.ADMIN){
                List<User> users = userService.getAllUsersByAdmin(leadId);
                users.stream().forEach(user1 -> {
                    userService.deleteUser(user1);
                });
            }
            userService.deleteUser(user.get());
            authHandler.logoutHandler(user.get().getEmail());
        }

    }

    public void deleteSubUser(Long id, UserDTO userDTO){
        List<UserDTO> users = getUsers(id);
        UserDTO userDTO1 = users.stream().filter(user -> user.getEmail().equals(userDTO.getEmail()))
                .findFirst().orElseThrow(() -> new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL));
        Optional<User> user  = userService.getUser(userDTO1);
        if (user.isPresent()) {
            userService.deleteUser(user.get());
        }else {
            throw new UserException(ErrorCode.USER_NOT_PRESENT_WITH_EMAIL);
        }

    }
    @Override
    public void bulkUpload(MultipartFile file,Long id) {
        List<User> users = userExcelHelper.processExcelData(file);
        users.stream().forEach(user -> {
            if(userRepo.existsByEmail(user.getEmail())) throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
            user.setRegisteredBy(id);
            user.setRegisteredOn(java.time.LocalDateTime.now());
            userService.registerUser(user);
        });
    }

}


