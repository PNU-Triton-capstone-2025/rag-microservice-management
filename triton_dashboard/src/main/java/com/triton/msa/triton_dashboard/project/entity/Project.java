package com.triton.msa.triton_dashboard.project.entity;

import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.user.entity.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    private SshInfo sshInfo;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RagHistory> ragHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrivateData> privateData = new ArrayList<>();

    public void updateSshInfo(SshInfo sshInfo) {
        this.sshInfo = sshInfo;
    }

    public void linkUser(User user) {
        this.user = user;
    }

    public Long fetchId() {
        return id;
    }

    public String fetchName() {
        return name;
    }

    public SshInfo fetchSshInfo() {
        return sshInfo;
    }

    protected Project() {

    }

    public Project(String name) {
        this.name = name;
    }
}
