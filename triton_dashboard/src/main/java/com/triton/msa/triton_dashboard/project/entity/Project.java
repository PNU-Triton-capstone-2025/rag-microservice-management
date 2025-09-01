package com.triton.msa.triton_dashboard.project.entity;

import com.triton.msa.triton_dashboard.common.converter.SavedYamlListConverter;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.user.entity.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
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

    @Embedded
    private LogAnalysisModel logAnalysisEndpoint;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RagHistory> ragHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrivateData> privateData = new ArrayList<>();

    @Convert(converter = SavedYamlListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<SavedYaml> savedYamls = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public void updateSshInfo(SshInfo sshInfo) {
        this.sshInfo = sshInfo;
    }
    public void updateLogAnalysisEndpoint(LogAnalysisModel logAnalysisEndpoint) {
        this.logAnalysisEndpoint = logAnalysisEndpoint;
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

    public User fetchUser() {
        return user;
    }
    public LogAnalysisModel fetchEndpoint() {
        return logAnalysisEndpoint;
    }

    public LocalDateTime fetchCreatedAt() {
        return this.createdAt;
    }

    public List<SavedYaml> fetchSavedYamls() {
        return this.savedYamls;
    }

    protected Project() {

    }

    public Project(String name) {
        this.name = name;
    }
}
