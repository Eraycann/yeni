package org.kafka.evrak.repository;

import org.kafka.evrak.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {


    Optional<Company> findByNameAndIsActive(String name, boolean isActive);
}
