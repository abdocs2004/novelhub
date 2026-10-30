package com.novelhub.repository;

import com.novelhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Extending JpaRepository gives us save(), findById(), findAll(), delete(), count() ... for free.
 * Spring Data also builds queries from method names: findByEmail => "select u from User u where u.email = ?".
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
