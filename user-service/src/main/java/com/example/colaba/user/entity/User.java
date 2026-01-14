package com.example.colaba.user.entity;

import com.example.colaba.shared.common.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "password")
@EqualsAndHashCode(exclude = "password")
public class User {
    @Id
    private Long id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("password")
    @JsonIgnore
    private String password;

    @Column("role")
    private UserRole role;

    @Column("created_at")
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private OffsetDateTime updatedAt;

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
