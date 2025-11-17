package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.dto.UserDTO;
import com.example.crm_system_backend.constants.Roles;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.IUserRepo;
import com.example.crm_system_backend.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepo userRepo;

    /**
     * Registers a new user by saving their details to the repository.
     *
     * @param user the user entity containing the details to be registered
     * @return the registered user entity after being saved to the repository
     */
    @Override
    public User registerUser(User user) {
        return userRepo.save(user);
    }

    /**
     * Updates the details of an existing user in the system.
     * <p>
     * This method is responsible for modifying user information stored in the repository.
     * The specific update operation depends on the implementation
     */
    @Override
    public void updateUser() {

    }

    /**
     * Deletes a specified user from the repository.
     *
     * @param user the user entity to be deleted
     */
    @Override
    public void deleteUser(User user) {

        userRepo.delete(user);
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return a list of User entities, or an empty list if no users are found
     */
    @Override
    public List<User> getAllUsers() {
        return List.of();
    }

    /**
     * Retrieves a list of all users associated with a given master admin.
     * <p>
     * This method first fetches all users registered by the master admin. It then
     * identifies users with an admin role and recursively fetches the users registered
     * by those admins. The final list contains all users associated with the master admin,
     * including those registered by other admins.
     *
     * @param id the ID of the master admin whose associated users are to be retrieved
     * @return a list of User entities associated with the given master admin
     */
    @Override
    public List<User> getAllUserByMasterAdmin(Long id) {
        log.info("Request for getting users is in user Handler for master admin");
        List<User> user = new ArrayList<>();
        List<User> users = userRepo.findUsersByRegisteredBy(id);
        user.addAll(users);
        log.info("users:{}", users);
        List<User> admins = users.stream()
                .filter(user1 -> user1.getRole() == Roles.ADMIN)
                .toList();
        log.info("admins:{}", admins, "Registered by ", id);
        admins.forEach(user1 -> {
            List<User> users2 = getAllUsersByAdmin(user1.getId());
            user.addAll(users2);
            log.info("users2:{}", users2);
        });
        return user;
    }

    /**
     * Retrieves a list of all users registered by a specific admin.
     *
     * @param id
     */
    @Override
    public List<User> getAllUsersByAdmin(Long id) {
        return userRepo.findUsersByRegisteredBy(id);
    }

    /**
     * Retrieves a user from the repository based on the provided email and password.
     *
     **/
    @Override
    public Optional<User> getUser(UserDTO dto) {
        return userRepo.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    /**
     * Checks if a user exists in the repository based on the provided email.
     *
     * @param email the email address of the user to check
     * @return true if a user with the specified email exists, false otherwise
     */
    @Override
    public boolean checkUserByEmail(String email) {

        return userRepo.existsByEmail(email);
    }

    /**
     * Checks if a user exists in the repository based on their mobile number.
     *
     * @param number the mobile number of the user to check
     * @return true if a user with the specified mobile number exists, false otherwise
     */
    @Override
    public boolean checkUserByMobileNumber(String number) {

        return userRepo.existsByMobileNumber(number);
    }

    /**
     * Retrieves a user from the repository based on the provided user ID.
     *
     * @param id the unique ID of the user to be retrieved
     */
    public Optional<User> getUserById(Long id) {
        return userRepo.findById(id);
    }

    /**
     * This method is intended to retrieve all users in the system.
     * <p>
     * The specific behavior, return type, and implementation of this method
     * may vary based on the context in which it is used.
     */
    @Override
    public void getAllUser() {

    }

    /**
     * Retrieves a user from the repository based on the provided email address.
     *
     * @param email the email address of the user to be retrieved
     * @return an Optional containing the user entity if found, or an empty Optional if no user is found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepo.getUserByEmail(email);
    }

    public Optional<List<User>> getAllUsersRegisterById(Long id) {
        List<User> users = userRepo.findUsersByRegisteredBy(id);
        return Optional.ofNullable(users);
    }
}
