package com.appfinace.api.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

import com.appfinace.api.domain.user.User;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldReturnTrueWhenEmailExists() {
        User user = new User();

        user.setEmail("test@email.com");
        user.setName("Test");
        user.setPassword("123");
        user.setCurrentProfileImgUrl("//image");
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("test@email.com")).isTrue();
    }

    @Test
    public void shouldFilterUsersByPartialName() {
        User user = new User();

        user.setEmail("test@email.com");
        user.setName("Test");
        user.setPassword("123");
        user.setCurrentProfileImgUrl("//image");
        userRepository.save(user);

        var result = userRepository.getFiltredUsers("tes", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
