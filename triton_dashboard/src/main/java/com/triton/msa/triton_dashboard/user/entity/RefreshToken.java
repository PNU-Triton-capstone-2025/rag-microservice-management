package com.triton.msa.triton_dashboard.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    protected RefreshToken() {

    }

    public RefreshToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public String retrieveToken() {
        return this.token;
    }

    public RefreshToken updateToken(String newToken) {
        this.token = newToken;
        return this;
    }
}
