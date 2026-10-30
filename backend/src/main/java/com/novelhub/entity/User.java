package com.novelhub.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Maps to the "users" table. (We avoid the name "user" because it is a reserved word in PostgreSQL.)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL auto-increment column
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Always stores the BCrypt hash, never the raw password.
    @Column(nullable = false)
    private String password;

    // Stored as text ("USER"/"ADMIN"). Never use ORDINAL: reordering the enum would corrupt data.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
}
