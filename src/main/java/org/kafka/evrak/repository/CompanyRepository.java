package org.kafka.evrak.repository;

import org.kafka.evrak.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByNameAndIsActive(String name, boolean isActive);

    Page<Company> findByIsActive(boolean isActive, Pageable pageable);

    // Artık güncelleme sırasında duplicate kontrolü için ayrı bir metod kullanmaya gerek kalmıyor.
    boolean existsByNameAndIsActive(String name, boolean isActive);
}
