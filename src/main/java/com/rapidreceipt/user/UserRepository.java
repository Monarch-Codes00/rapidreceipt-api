package com.rapidreceipt.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for User entities.
 *
 * Spring Data JPA auto-generates the SQL for all these methods at startup —
 * no implementation needed. Method names are parsed by Spring using conventions:
 * findBy<Field>, existsBy<Field>, countBy<Field>, etc.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used by AuthService to look up a user during login and registration. */
    Optional<User> findByEmail(String email);

    /** Used by AuthService to check for duplicate emails before registering. */
    boolean existsByEmail(String email);
}
