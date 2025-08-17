package com.fullnestjob.modules.companies.service;

import com.fullnestjob.modules.companies.dto.CompanyDtos.CompanyDetailDTO;
import com.fullnestjob.modules.companies.dto.CompanyDtos.CreateCompanyBodyDTO;
import com.fullnestjob.modules.companies.dto.CompanyDtos.UpdateCompanyBodyDTO;
import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import com.fullnestjob.modules.jobs.repo.JobRepository;
import com.fullnestjob.modules.jobs.entity.Job;
import java.util.List;
import com.fullnestjob.modules.resumes.repo.ResumeRepository;
import com.fullnestjob.modules.resumes.entity.Resume;
import com.fullnestjob.modules.users.repo.UserRepository;

@Service
@Transactional(readOnly = true)
public class CompaniesService {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private UserRepository userRepository;

    public PageResultDTO<CompanyDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Sort sort = parseSort(query);
        PageRequest pageable = PageRequest.of(current - 1, pageSize, sort);
        Page<Company> page;
        String nameFilter = query.getName();
        String addressFilter = query.getAddress();
        String scope = query.getScope();
        boolean forcePublic = scope != null && scope.equalsIgnoreCase("public");
        if (nameFilter != null) nameFilter = nameFilter.replaceAll("^/+|/i$", "");
        if (addressFilter != null) addressFilter = addressFilter.replaceAll("^/+|/i$", "");

        String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        boolean isAdmin = currentRole != null && (currentRole.equalsIgnoreCase("ADMIN") || currentRole.equalsIgnoreCase("SUPER_ADMIN"));
        if (forcePublic) {
            page = companyRepository.findPublicCompanies(nameFilter, addressFilter, pageable);
        } else if (!isAdmin) {
            String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                // Public (chưa đăng nhập) => chỉ company chưa xóa
                page = companyRepository.findPublicCompanies(nameFilter, addressFilter, pageable);
            } else {
                // Non-admin logged-in: chỉ xem công ty của mình
                var me = this.userRepository.findById(currentUserId).orElse(null);
                String companyId = me != null && me.getCompany() != null ? me.getCompany().get_id() : null;
                if (companyId != null) {
                    java.util.Optional<com.fullnestjob.modules.companies.entity.Company> c = companyRepository.findById(companyId);
                    java.util.List<Company> list = c.map(java.util.List::of).orElse(java.util.List.of());
                    org.springframework.data.domain.PageImpl<Company> p = new org.springframework.data.domain.PageImpl<>(list, pageable, list.size());
                    page = p;
                } else {
                    page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
                }
            }
        } else {
            if (nameFilter != null && addressFilter != null) {
                page = companyRepository.findByNameContainingIgnoreCaseAndAddressContainingIgnoreCase(nameFilter, addressFilter, pageable);
            } else if (nameFilter != null) {
                page = companyRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
            } else if (addressFilter != null) {
                page = companyRepository.findByAddressContainingIgnoreCase(addressFilter, pageable);
            } else {
                page = companyRepository.findAll(pageable);
            }
        }

        PageResultDTO<CompanyDetailDTO> res = new PageResultDTO<>();
        res.result = page.getContent().stream().map(this::toDetail).collect(Collectors.toList());
        MetaDTO meta = new MetaDTO();
        meta.current = current;
        meta.pageSize = pageSize;
        meta.total = (int) page.getTotalElements();
        meta.pages = page.getTotalPages();
        res.meta = meta;
        return res;
    }

    private Sort parseSort(PaginationQueryDTO query) {
        if (query == null || query.getPageSize() == null) {
            return Sort.by(Sort.Order.desc("updatedAt"));
        }
        String s = query.getSort();
        if (s != null && !s.isBlank()) {
            String key = s.startsWith("sort=") ? s.substring(5) : s;
            if (key.startsWith("-")) return Sort.by(Sort.Order.desc(key.substring(1)));
            return Sort.by(Sort.Order.asc(key));
        }
        return Sort.by(Sort.Order.desc("updatedAt"));
    }

    public CompanyDetailDTO findById(String id) {
        return companyRepository.findById(id).map(this::toDetail).orElse(null);
    }

    @Transactional
    public CompanyDetailDTO create(CreateCompanyBodyDTO body) {
        Company c = new Company();
        c.setName(body.name);
        c.setDescription(body.description);
        c.setAddress(body.address);
        c.setLogo(body.logo);
        Company saved = companyRepository.save(c);
        return toDetail(saved);
    }

    @Transactional
    public CompanyDetailDTO update(String id, UpdateCompanyBodyDTO body) {
        Company c = companyRepository.findById(id).orElseThrow();
        if (body.name != null) c.setName(body.name);
        if (body.description != null) c.setDescription(body.description);
        if (body.address != null) c.setAddress(body.address);
        if (body.logo != null) c.setLogo(body.logo);
        Company saved = companyRepository.save(c);
        return toDetail(saved);
    }

    @Transactional
    public void delete(String id) {
        // Ensure no users/jobs/resumes reference this company before delete to avoid FK constraint violation
        // 1) users.company -> null
        java.util.List<com.fullnestjob.modules.users.entity.User> users = userRepository.findAll();
        for (var u : users) {
            if (u.getCompany() != null && id.equals(u.getCompany().get_id())) {
                u.setCompany(null);
            }
        }
        userRepository.saveAll(users);

        // 2) jobs.company -> null
        List<Job> jobs = jobRepository.findByCompanyId(id);
        for (Job j : jobs) {
            j.setCompany(null);
        }
        if (!jobs.isEmpty()) {
            jobRepository.saveAll(jobs);
        }
        // 3) resumes.companyId -> null
        List<Resume> resumes = resumeRepository.findByCompanyId(id);
        for (Resume r : resumes) {
            r.setCompanyId(null);
        }
        if (!resumes.isEmpty()) {
            resumeRepository.saveAll(resumes);
        }
        companyRepository.deleteById(id);
    }

    private CompanyDetailDTO toDetail(Company c) {
        CompanyDetailDTO dto = new CompanyDetailDTO();
        dto._id = c.get_id();
        dto.name = c.getName();
        dto.description = c.getDescription();
        dto.address = c.getAddress();
        dto.logo = c.getLogo();
        dto.createdAt = c.getCreatedAt();
        dto.updatedAt = c.getUpdatedAt();
        dto.deletedAt = c.getDeletedAt();
        return dto;
    }
}


