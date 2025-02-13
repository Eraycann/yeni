package org.kafka.evrak.mapper;

import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.request.DtoDocumentIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.dto.response.DtoDocument;
import org.kafka.evrak.entity.Company;
import org.kafka.evrak.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "company", ignore = true)
    Document toEntity(DtoDocumentIU dto);

    @Mapping(target = "dtoCompany", ignore = true)
    DtoDocument toDto(Document document);

    @Mapping(target = "dtoCompany", ignore = true)
    List<DtoDocument> toDtoList(List<Document> documentList);
}
