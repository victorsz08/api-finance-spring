package com.appfinace.api.domain.user;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImages {

    @Id
    @GeneratedValue
    private UUID id;

    private String profileImageUrl;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user; 
}
