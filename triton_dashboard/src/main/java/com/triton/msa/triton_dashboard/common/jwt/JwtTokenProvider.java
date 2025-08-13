package com.triton.msa.triton_dashboard.common.jwt;

import org.springframework.beans.factory.annotation.Value;

import java.security.Key;

public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretkey;

    @Value("${jwt.expiration-time}")
    private long tokenValidTime;

    private Key key;
}
