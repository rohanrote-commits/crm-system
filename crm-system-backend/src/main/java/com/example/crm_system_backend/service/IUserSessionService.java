package com.example.crm_system_backend.service;

import com.example.crm_system_backend.entity.UserSession;

public interface IUserSessionService {
    public boolean findSessionByEmail(String email);
    public void deleteSessionByEmail(String email);
    public void saveSession(UserSession userSession);
    public boolean findSessionByToken(String token,String email);
}
