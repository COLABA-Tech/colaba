package com.example.colaba.user.repository;

import com.example.colaba.user.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username AND id != :id")
    Mono<Boolean> existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email AND id != :id")
    Mono<Boolean> existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}