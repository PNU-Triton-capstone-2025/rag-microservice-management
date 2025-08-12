package com.triton.msa.triton_dashboard.ssh.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Embeddable
@Getter
@Setter
@Slf4j
public class SshInfo {

    private String sshIpAddress;
    private String username;
    @Lob
    private String sshAuthKey;

    protected SshInfo() {

    }

    public SshInfo(String sshIpAddress, String username, String encryptedSshAuthKey) {
        this.sshIpAddress = sshIpAddress;
        this.username = username;
        this.sshAuthKey = encryptedSshAuthKey;
    }
}
