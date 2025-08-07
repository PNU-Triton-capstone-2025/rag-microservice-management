package com.triton.msa.triton_dashboard.private_data.repository;

import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateDataRepository extends JpaRepository<PrivateData, Long> {
    List<PrivateData> findByProjectId(Long projectId);
    Optional<PrivateData> findByIdAndProjectId(Long id, Long projectId);
    void deleteById(Long dataId);
    boolean existsByProjectIdAndFilename(Long projectId, String filename);
}