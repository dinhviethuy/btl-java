package com.fullnestjob.modules.companies.controller;

import com.fullnestjob.common.response.Message;
import com.fullnestjob.modules.companies.dto.CompanyDtos.CreateCompanyBodyDTO;
import com.fullnestjob.modules.companies.dto.CompanyDtos.CompanyDetailDTO;
import com.fullnestjob.modules.companies.dto.CompanyDtos.UpdateCompanyBodyDTO;
import com.fullnestjob.modules.companies.service.CompaniesService;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/companies")
public class CompaniesController {

    private final CompaniesService companiesService;

    public CompaniesController(CompaniesService companiesService) {
        this.companiesService = companiesService;
    }

    @GetMapping
    @Message("Companies fetched successfully")
    public ResponseEntity<PageResultDTO<CompanyDetailDTO>> find(@Valid @ModelAttribute PaginationQueryDTO query) {
        return ResponseEntity.ok(companiesService.find(query));
    }

    @GetMapping("/{id}")
    @Message("Company fetched successfully")
    public ResponseEntity<CompanyDetailDTO> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(companiesService.findById(id));
    }

    @PostMapping
    @Message("Company created successfully")
    public ResponseEntity<CompanyDetailDTO> create(@Valid @RequestBody CreateCompanyBodyDTO body) {
        return ResponseEntity.ok(companiesService.create(body));
    }

    @PatchMapping("/{id}")
    @Message("Company updated successfully")
    public ResponseEntity<CompanyDetailDTO> update(@PathVariable("id") String id, @RequestBody UpdateCompanyBodyDTO body) {
        return ResponseEntity.ok(companiesService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Message("Company deleted successfully")
    public ResponseEntity<CompanyDetailDTO> delete(@PathVariable("id") String id) {
        CompanyDetailDTO dto = companiesService.findById(id);
        companiesService.delete(id);
        return ResponseEntity.ok(dto);
    }
}


