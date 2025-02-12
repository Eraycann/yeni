package org.kafka.evrak.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.kafka.evrak.mapper.CompanyMapper;
import org.kafka.evrak.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private Company createCompany(DtoCompanyIU dto){
        return companyMapper.toEntity(dto);
    }

    /**
     * SAVE COMPANY
     */
    public DtoCompany saveCompany(DtoCompanyIU dto){
        Company company = createCompany(dto);
        return null;
    }
}
