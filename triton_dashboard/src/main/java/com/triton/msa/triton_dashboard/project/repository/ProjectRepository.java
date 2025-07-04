package com.triton.msa.triton_dashboard.project.repository;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
}
