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

    @Override
    public boolean findSessionByEmail(String email) {
        log.info("Enter : UserSessionService:findSessionByEmail");
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:findSessionByEmail");
        return userSessionRepo.existsByEmail(email);
    }


    @Override
    @Transactional
    public void deleteSessionByEmail(String email) {
        log.info("Enter : UserSessionService:deleteSessionByEmail");
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:deleteSessionByEmail");
        userSessionRepo.deleteByEmail(email);
    }

    @Override
    public void saveSession(UserSession userSession) {
        log.info("Enter : UserSessionService:saveSession");
        log.info("userSession:{}", userSession);
        log.info("Exit : UserSessionService:saveSession");
        userSessionRepo.save(userSession);
    }

    @Override
    public boolean findSessionByToken(String token, String email) {
        log.info("Enter : UserSessionService:findSessionByToken");
        log.info("token:{}", token);
        log.info("email:{}", email);
        log.info("Exit : UserSessionService:findSessionByToken");
        return userSessionRepo.existsByTokenAndEmail(token, email);
    }


}
