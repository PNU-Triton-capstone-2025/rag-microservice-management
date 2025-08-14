package com.triton.msa.triton_dashboard.private_data.repository;

import com.triton.msa.triton_dashboard.private_data.dto.ProjectPrivateDataDto;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivateDataRepository extends JpaRepository<PrivateData, Long> {
    Optional<PrivateData> findByIdAndProjectId(Long id, Long projectId);
    void deleteById(Long dataId);
    boolean existsByProjectIdAndFilename(Long projectId, String filename);

    @Query("""
        select new com.triton.msa.triton_dashboard.private_data.dto.ProjectPrivateDataDto(
            pd.id, p.id, pd.filename, pd.contentType, pd.createdAt
        )
        from PrivateData pd
        join pd.project p
        where p.id = :projectId
        order by pd.createdAt desc
    """)
    List<ProjectPrivateDataDto> getPrivateDataDtosByProjectId(@Param("projectId") Long projectId);
}