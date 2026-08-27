package com.appfinace.api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appfinace.api.domain.user.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    public Boolean existByEmail(String email);
}
