package com.bank.authservice.repository;

import com.bank.authservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WhenUserExists_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .customerId("customer123")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.ACTIVE)
                .build();

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getCustomerId()).isEqualTo("customer123");
    }

    @Test
    void findByUsername_WhenUserNotExists_ReturnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findByCustomerId_WhenUserExists_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .customerId("customer456")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.ACTIVE)
                .build();

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByCustomerId("customer456");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void existsByUsername_WhenUserExists_ReturnsTrue() {
        User user = User.builder()
                .username("existinguser")
                .password("encodedPassword")
                .customerId("customer789")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.ACTIVE)
                .build();

        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByUsername("existinguser");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WhenUserNotExists_ReturnsFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByCustomerId_WhenCustomerHasAccount_ReturnsTrue() {
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .customerId("existingcustomer")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.ACTIVE)
                .build();

        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByCustomerId("existingcustomer");

        assertThat(exists).isTrue();
    }

    @Test
    void save_User_PersistsUser() {
        User user = User.builder()
                .username("newuser")
                .password("encodedPassword")
                .customerId("newcustomer")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.PENDING_ACTIVATION)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
    }
}
