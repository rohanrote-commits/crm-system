package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.entity.UserSession;
import com.example.crm_system_backend.repository.UserSessionRepo;
import com.example.crm_system_backend.service.IUserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSessionService implements IUserSessionService {
    @Autowired
    private UserSessionRepo userSessionRepo;
    @Override
    public boolean findSessionByEmail(String email) {
        return userSessionRepo.existsByEmail(email);
    }



    @Override
    @Transactional
    public void deleteSessionByEmail(String email) {
           userSessionRepo.deleteByEmail(email);
    }

    @Override
    public void saveSession(UserSession userSession) {
        userSessionRepo.save(userSession);
    }
}
