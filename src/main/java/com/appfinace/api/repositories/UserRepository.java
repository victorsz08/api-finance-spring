package com.appfinace.api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.appfinace.api.domain.user.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    public Boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);

    @Query("SELECT a FROM User a " +
            "WHERE (:name IS NULL OR a.name ILIKE %:name%) AND " +
            "(:email IS NULL OR a.email ILIKE %:email%)")
    public Page<User> getFiltredUsers(
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);

}
