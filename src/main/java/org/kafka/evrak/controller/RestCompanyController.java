package org.kafka.evrak.controller;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.service.CompanyService;
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
    public RootEntity<List<DtoCompany>> getAllActiveCompanies() {
        return ok(companyService.getAllActiveCompanies());
    }

    @GetMapping("/getAllInactive")
    public RootEntity<List<DtoCompany>> getAllInactiveCompanies() {
        return ok(companyService.getAllInactiveCompanies());
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
}
