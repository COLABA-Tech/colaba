package com.example.colaba.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private UserRole role;

    public User(String username, String email, UserRole role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
