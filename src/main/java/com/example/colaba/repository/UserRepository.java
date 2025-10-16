package com.example.colaba.repository;

import com.example.colaba.entity.User;
import com.example.colaba.entity.UserRole;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u ORDER BY u.id LIMIT ?2 OFFSET ?1")
    Slice<User> findAllByOffset(long offset, int limit);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}