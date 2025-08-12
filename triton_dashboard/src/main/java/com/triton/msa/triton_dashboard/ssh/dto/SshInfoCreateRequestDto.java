package com.triton.msa.triton_dashboard.ssh.dto;

import org.springframework.web.multipart.MultipartFile;

public record SshInfoCreateRequestDto(
        String sshIpAddress,
        String username,
        MultipartFile pemFile
) {
    private static final String DEFAULT_SSH_IP_ADDRESS = "127.0.0.1";
    private static final String DEFAULT_USERNAME = "username";

    public static SshInfoCreateRequestDto getEmpty() {
        return new SshInfoCreateRequestDto(
                DEFAULT_SSH_IP_ADDRESS,
                DEFAULT_USERNAME,
                null
        );
    }
}
