package com.triton.msa.triton_dashboard.project.entity;

import com.triton.msa.triton_dashboard.chat_history.entity.ChatHistory;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    private SshInfo sshInfo;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatHistory> chatHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrivateData> privateData = new ArrayList<>();

    public void updateSshInfo(SshInfo sshInfo) {
        this.sshInfo = sshInfo;
    }

    protected Project() {

    }

    public Project(String name) {
        this.name = name;
    }
}
