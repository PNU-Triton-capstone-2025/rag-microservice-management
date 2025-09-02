package com.triton.msa.triton_dashboard.project.repository;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);

    @Query("SELECT p FROM Project p JOIN FETCH p.user WHERE p.id = :projectId")
    Optional<Project> findByIdWithUser(@Param("projectId") Long projectId);
}
