package com.triton.msa.triton_dashboard.user.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_api_keys", joinColumns = @JoinColumn(name = "user_id"))
    private Set<ApiKeyInfo> apiKeys = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    public void addProject(Project project) {
        projects.add(project);
        project.linkUser(this);
    }

    protected User() {}

    public User(String username, String password, Set<ApiKeyInfo> apiKeys, Set<UserRole> roles) {
        this.username = username;
        this.password = password;
        this.apiKeys = apiKeys;
        this.roles = roles;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
