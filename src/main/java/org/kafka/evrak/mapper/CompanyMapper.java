package org.kafka.evrak.mapper;

import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true) // BaseEntity'deki isActive field'ı için
    @Mapping(target = "folderPath", ignore = true)
    Company toEntity(DtoCompanyIU dto);

    DtoCompany toDto(Company company);

    List<DtoCompany> toDtoList(List<Company> companyList);
}
