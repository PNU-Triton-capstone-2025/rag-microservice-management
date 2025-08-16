package com.triton.msa.triton_dashboard.ssh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;

public record SshInfoCreateRequestDto(
        @JsonProperty("ssh_ip_address")
        String sshIpAddress,
        String username,
        @JsonProperty("pem_file")
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
