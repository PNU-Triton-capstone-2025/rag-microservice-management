package com.triton.msa.triton_dashboard.project.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class SshInfo {

    private String sshIpAddress;
    @Lob
    private String sshAuthKey;
}
