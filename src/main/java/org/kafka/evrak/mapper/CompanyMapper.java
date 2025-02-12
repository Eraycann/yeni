package org.kafka.evrak.mapper;

import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // DTO'dan Entity'ye dönüşüm (Create & Update için)
    @Mapping(target = "id", ignore = true) // ID create sırasında set edilmez
    @Mapping(target = "createdAt", ignore = true) // Otomatik oluşturulur
    @Mapping(target = "updatedAt", ignore = true) // Güncellenir
    @Mapping(target = "isActive", ignore = true) // Varsayılan true
    Company toEntity(DtoCompanyIU dto);

    // Entity'den DTO'ya dönüşüm (Response için)
    DtoCompany toDto(Company company);
}
