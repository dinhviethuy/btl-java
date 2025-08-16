package com.fullnestjob.modules.jobs.service;

import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import com.fullnestjob.modules.jobs.dto.JobDtos.CreateJobBodyDTO;
import com.fullnestjob.modules.jobs.dto.JobDtos.JobDetailDTO;
import com.fullnestjob.modules.jobs.dto.JobDtos.UpdateJobBodyDTO;
import com.fullnestjob.modules.jobs.entity.Job;
import com.fullnestjob.modules.jobs.repo.JobRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class JobsService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobsService(JobRepository jobRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    public PageResultDTO<JobDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Sort sort = parseSort(query);
        PageRequest pageable = PageRequest.of(current - 1, pageSize, sort);
        Page<Job> page;
        String nameFilter = query.getName();
        String locationFilter = query.getLocation();
        if (nameFilter != null) nameFilter = nameFilter.replaceAll("^/+|/i$", "");
        if (locationFilter != null) locationFilter = locationFilter.replaceAll("^/+|/i$", "");
        if (nameFilter != null && locationFilter != null) {
            page = jobRepository.findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(nameFilter, locationFilter, pageable);
        } else if (nameFilter != null) {
            page = jobRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else if (locationFilter != null) {
            page = jobRepository.findByLocationContainingIgnoreCase(locationFilter, pageable);
        } else {
            page = jobRepository.findAll(pageable);
        }
        PageResultDTO<JobDetailDTO> res = new PageResultDTO<>();
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
        if (query == null) return Sort.by(Sort.Order.desc("updatedAt"));
        try {
            java.lang.reflect.Field f = query.getClass().getDeclaredField("sort");
            f.setAccessible(true);
            Object v = f.get(query);
            if (v instanceof String s && !s.isBlank()) {
                String key = s.startsWith("sort=") ? s.substring(5) : s;
                if (key.startsWith("-")) return Sort.by(Sort.Order.desc(key.substring(1)));
                return Sort.by(Sort.Order.asc(key));
            }
        } catch (Exception ignored) {}
        return Sort.by(Sort.Order.desc("updatedAt"));
    }

    public JobDetailDTO findById(String id) {
        return jobRepository.findById(id).map(this::toDetail).orElse(null);
    }

    @Transactional
    public JobDetailDTO create(CreateJobBodyDTO body) {
        Job j = new Job();
        j.setLocation(body.location);
        j.setName(body.name);
        j.setSkills(body.skills);
        j.setSalary(body.salary);
        j.setQuantity(body.quantity);
        j.setLevel(body.level);
        j.setDescription(body.description);
        j.setStartDate(body.startDate);
        j.setEndDate(body.endDate);
        j.setIsActive(body.isActive != null ? body.isActive : Boolean.TRUE);
        if (body.company != null && body.company._id != null) {
            Company c = companyRepository.findById(body.company._id).orElse(null);
            j.setCompany(c);
        }
        return toDetail(jobRepository.save(j));
    }

    @Transactional
    public JobDetailDTO update(String id, UpdateJobBodyDTO body) {
        Job j = jobRepository.findById(id).orElseThrow();
        if (body.location != null) j.setLocation(body.location);
        if (body.name != null) j.setName(body.name);
        if (body.skills != null) j.setSkills(body.skills);
        if (body.salary != null) j.setSalary(body.salary);
        if (body.quantity != null) j.setQuantity(body.quantity);
        if (body.level != null) j.setLevel(body.level);
        if (body.description != null) j.setDescription(body.description);
        if (body.startDate != null) j.setStartDate(body.startDate);
        if (body.endDate != null) j.setEndDate(body.endDate);
        if (body.isActive != null) j.setIsActive(body.isActive);
        if (body.company != null && body.company._id != null) {
            Company c = companyRepository.findById(body.company._id).orElse(null);
            j.setCompany(c);
        }
        return toDetail(jobRepository.save(j));
    }

    @Transactional
    public void delete(String id) {
        jobRepository.deleteById(id);
    }

    private JobDetailDTO toDetail(Job j) {
        JobDetailDTO dto = new JobDetailDTO();
        dto._id = j.get_id();
        dto.location = j.getLocation();
        dto.name = j.getName();
        dto.skills = j.getSkills() != null ? new java.util.ArrayList<>(j.getSkills()) : java.util.Collections.emptyList();
        dto.salary = j.getSalary();
        dto.quantity = j.getQuantity();
        dto.level = j.getLevel();
        dto.description = j.getDescription();
        dto.startDate = j.getStartDate();
        dto.endDate = j.getEndDate();
        dto.isActive = j.getIsActive();
        if (j.getCompany() != null) {
            dto.company = new com.fullnestjob.modules.jobs.dto.JobDtos.CompanyNestedDTO();
            dto.company._id = j.getCompany().get_id();
            dto.company.name = j.getCompany().getName();
            dto.company.logo = j.getCompany().getLogo();
        }
        dto.createdAt = j.getCreatedAt();
        dto.updatedAt = j.getUpdatedAt();
        dto.deletedAt = j.getDeletedAt();
        return dto;
    }
}


