package org.kafka.evrak.repository;

import org.kafka.evrak.entity.Company;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByNameAndIsActive(String name, boolean isActive);

    // Veritabanı seviyesinde isActive filtrelemesi yapar ve sıralı sonuç döner.
    List<Company> findByIsActive(boolean isActive, Sort sort);

    // Artık güncelleme sırasında duplicate kontrolü için ayrı bir metod kullanmaya gerek kalmıyor.
    boolean existsByNameAndIsActive(String name, boolean isActive);
}
