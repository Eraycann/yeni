package org.kafka.evrak.mapper;

import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // DTO'dan Entity'ye dönüşüm (Create & Update senaryoları için)
    @Mapping(target = "id", ignore = true)         // Create sırasında id otomatik oluşturulur
    @Mapping(target = "createdAt", ignore = true)  // Auditing mekanizması tarafından doldurulur
    @Mapping(target = "updatedAt", ignore = true)  // Auditing mekanizması tarafından doldurulur
    @Mapping(target = "isActive", ignore = true)   // Varsayılan değer entity'de tanımlı
    @Mapping(target = "folderPath", ignore = true)
    Company toEntity(DtoCompanyIU dto);

    // Entity'den DTO'ya dönüşüm (Response için)
    DtoCompany toDto(Company company);
}
