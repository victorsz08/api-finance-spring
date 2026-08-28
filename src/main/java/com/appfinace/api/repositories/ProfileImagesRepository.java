package com.appfinace.api.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appfinace.api.domain.user.ProfileImages;

public interface ProfileImagesRepository extends JpaRepository<ProfileImages, UUID>{
    public List<ProfileImages> findByUserId(UUID userId);
}
