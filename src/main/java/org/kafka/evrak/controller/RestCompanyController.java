package org.kafka.evrak.controller;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/companies")
public class RestCompanyController extends RestBaseController{

    private final CompanyService companyService;

    @PostMapping("/save")
    public RootEntity<DtoCompany> saveCompany(@RequestBody DtoCompanyIU dtoCompanyIU) {
        return ok(companyService.saveCompany(dtoCompanyIU));
    }

    @PutMapping("/update/{id}")
    public RootEntity<DtoCompany> updateCompany(@PathVariable Long id, @RequestBody DtoCompanyIU dtoCompanyIU) {
        return ok(companyService.updateCompany(id, dtoCompanyIU));
    }

    @GetMapping("/getAllActive")
    public RootEntity<Page<DtoCompany>> getAllActiveCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ok(companyService.getAllActiveCompanies(page, size));
    }

    @GetMapping("/getAllInactive")
    public RootEntity<Page<DtoCompany>> getAllInactiveCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ok(companyService.getAllInactiveCompanies(page, size));
    }

    @PutMapping("/activate/{id}")
    public RootEntity<Long> activateCompany(@PathVariable Long id) {
        return ok(companyService.activateCompany(id));
    }

    @PutMapping("/deactivate/{id}")
    public RootEntity<Long> deactivateCompany(@PathVariable Long id) {
        return ok(companyService.deactivateCompany(id));
    }

    @GetMapping("/getActiveByName")
    public RootEntity<DtoCompany> getActiveCompaniesByName(@RequestParam(required = true) @Size(max = 100) String name) {
        return ok(companyService.getActiveCompaniesByName(name));
    }

    @GetMapping("/getInactiveByName")
    public RootEntity<DtoCompany> getInactiveCompaniesByName(@RequestParam(required = true) @Size(max = 100) String name) {
        return ok(companyService.getInactiveCompaniesByName(name));
    }

    /**
     * Şirketin içerisinde evrak yoksa, firmayı kalıcı olarak siler.
     * Dosya sistemi kontrolü yapılarak, klasörün adı "archived_" önekli olsun veya olmasın, klasör boşsa silinir.
     */
    @DeleteMapping("/delete/{id}")
    public RootEntity<Long> deleteCompanyPermanently(@PathVariable Long id) {
        return ok(companyService.deleteCompanyPermanently(id));
    }
}
