package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.repository.UserSessionRepo;
import com.example.crm_system_backend.service.IUserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserSessionService implements IUserSessionService {
    @Autowired
    private UserSessionRepo userSessionRepo;

    /**
     * Checks if a session exists for a user with the specified email address.
     *
     * @param email the email address of the user whose session existence needs to be verified
     * @return true if a session exists for the specified email, false otherwise
     */
    @Override
    public boolean findSessionByEmail(String email) {
        log.info("Enter : UserSessionService:findSessionByEmail");
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:findSessionByEmail");
        return userSessionRepo.existsByEmail(email);
    }

    /**
     * Deletes a user session associated with the provided email address.
     * <p>
     * This method removes the session data linked to the specified email
     * by delegating the operation to the repository layer. It operates within
     * a transactional context to ensure consistency and atomicity of the operation.
     *
     * @param email the email address of the user whose session is to be deleted
     */
    @Override
    @Transactional
    public void deleteSessionByEmail(String email) {
        log.info("Enter : UserSessionService:deleteSessionByEmail");
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:deleteSessionByEmail");
        userSessionRepo.deleteByEmail(email);
    }

    /**
     * Saves a user session into the repository.
     *
     * @param userSession the user session to be saved
     */
    @Override
    public void saveSession(UserSession userSession) {
        log.info("Enter : UserSessionService:saveSession");
        log.info("userSession:{}", userSession);
        log.info("Exit : UserSessionService:saveSession");
        userSessionRepo.save(userSession);
    }

    /**
     * Checks if a user session exists with the specified token and email address.
     *
     * @param token the unique token associated with the user session
     * @param email the email address of the user whose session existence needs to be verified
     * @return true if a session exists for the specified token and email, false otherwise
     */
    @Override
    public boolean findSessionByToken(String token, String email) {
        log.info("Enter : UserSessionService:findSessionByToken");
        log.info("token:{}", token);
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:findSessionByToken");
        return userSessionRepo.existsByTokenAndEmail(token, email);
    }


}
