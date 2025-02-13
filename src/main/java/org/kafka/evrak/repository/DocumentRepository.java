package org.kafka.evrak.repository;

import org.kafka.evrak.entity.Document;
import org.kafka.evrak.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCompanyIdAndIsActive(Long companyId, boolean isActive);

    @Query("SELECT d FROM Document d WHERE d.company.id = :companyId AND d.isActive = :active " +
            "AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:startDate IS NULL OR d.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR d.createdAt <= :endDate) " +
            "AND (:category IS NULL OR d.category = :category) " +
            "ORDER BY d.id DESC")
    List<Document> filterDocuments(@Param("companyId") Long companyId,
                                   @Param("active") boolean active,
                                   @Param("name") String name,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("category") DocumentCategory category);

    boolean existsByCompanyIdAndNameAndIsActive(Long companyId, String name, boolean isActive);
}
