package com.triton.msa.triton_dashboard.ssh.dto;

import org.springframework.web.multipart.MultipartFile;

public record SshInfoCreateRequestDto(
        String sshIpAddress,
        String username,
        MultipartFile pemFile
) {
}
