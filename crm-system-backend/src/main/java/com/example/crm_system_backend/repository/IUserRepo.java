package com.example.crm_system_backend.repository;

import com.example.crm_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface IUserRepo extends JpaRepository<User,Long> {

}
