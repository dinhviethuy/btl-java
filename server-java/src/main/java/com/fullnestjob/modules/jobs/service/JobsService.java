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
    private final com.fullnestjob.modules.users.repo.UserRepository userRepository;
    private final com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository;

    public JobsService(JobRepository jobRepository, CompanyRepository companyRepository, com.fullnestjob.modules.users.repo.UserRepository userRepository, com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
    }

    public PageResultDTO<JobDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Sort sort = parseSort(query);
        PageRequest pageable = PageRequest.of(current - 1, pageSize, sort);
        Page<Job> page;
        String nameFilter = query.getName();
        String locationFilter = query.getLocation();
        String scope = query.getScope();
        String companyIdFilter = query.getCompanyId();
        String excludeId = query.getExcludeId();
        String salaryStr = query.getSalary();
        Double minSalaryParam = query.getMinSalary();
        Double maxSalaryParam = query.getMaxSalary();
        String levelCsv = query.getLevel();
        Double minSalary = null;
        Double maxSalary = null;
        if (salaryStr != null && !salaryStr.isBlank()) {
            try {
                String cleaned = salaryStr.replaceAll("^/+|/i$", "").trim();
                String lowered = cleaned.toLowerCase();
                // Extract numeric part
                String numeric = lowered.replaceAll("[^0-9\\.]", "");
                if (!numeric.isBlank()) {
                    double base = Double.parseDouble(numeric);
                    double vnd;
                    if (lowered.contains("usd")) {
                        vnd = base * 24000d; // convert USD -> VND (approx)
                    } else if (lowered.endsWith("k")) {
                        vnd = base * 1_000d;
                    } else if (lowered.endsWith("m")) {
                        vnd = base * 1_000_000d;
                    } else if (base <= 1000d) {
                        // Assume millions VND if small integer like 10, 20, 200 (-> 10M, 20M, 200M)
                        vnd = base * 1_000_000d;
                    } else if (base <= 10000d) {
                        // Assume USD if in the 1k..10k range (e.g., 2000 -> $2000)
                        vnd = base * 24000d;
                    } else {
                        // Already VND
                        vnd = base;
                    }
                    minSalary = vnd;
                }
            } catch (Exception ignored) {}
        }
        if (minSalaryParam != null) minSalary = minSalaryParam;
        if (maxSalaryParam != null) maxSalary = maxSalaryParam;
        java.util.List<String> skillsFilter = query.getSkills();
        java.util.List<String> locationsFilter = query.getLocations();
        boolean hasSkills = skillsFilter != null && !skillsFilter.isEmpty();
        boolean hasLocations = locationsFilter != null && !locationsFilter.isEmpty();
        if (hasSkills) {
            skillsFilter = skillsFilter.stream().map(x -> x.toLowerCase()).collect(java.util.stream.Collectors.toList());
        }
        if (hasLocations) {
            locationsFilter = locationsFilter.stream().map(x -> x.toLowerCase()).collect(java.util.stream.Collectors.toList());
        }
        String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        boolean isAdmin = currentRole != null && (currentRole.equalsIgnoreCase("ADMIN") || currentRole.equalsIgnoreCase("SUPER_ADMIN"));
        boolean forcePublic = scope != null && scope.equalsIgnoreCase("public");
        boolean forceAdmin = scope != null && scope.equalsIgnoreCase("admin") && isAdmin;
        if (nameFilter != null) nameFilter = nameFilter.replaceAll("^/+|/i$", "");
        if (locationFilter != null) locationFilter = locationFilter.replaceAll("^/+|/i$", "");
        if (companyIdFilter != null && !companyIdFilter.isBlank()) {
            // explicit filter by company id for public/company detail page
            if (forcePublic || !(isAdmin || forceAdmin)) {
                // Only return active + within date window for public scope
                page = jobRepository.findPublicJobsByCompanyId(companyIdFilter, pageable);
            } else {
                page = jobRepository.findByCompanyId(companyIdFilter, pageable);
            }
        } else if (forcePublic) {
            if (hasSkills && hasLocations) {
                page = jobRepository.findPublicJobsBySkillsAndLocations(nameFilter, skillsFilter, locationsFilter, excludeId, pageable);
            } else if (hasSkills) {
                page = jobRepository.findPublicJobsBySkills(nameFilter, locationFilter, skillsFilter, excludeId, pageable);
            } else if (hasLocations) {
                page = jobRepository.findPublicJobsByLocations(nameFilter, locationsFilter, excludeId, pageable);
            } else {
                page = jobRepository.findPublicJobs(nameFilter, locationFilter, excludeId, pageable);
            }
        } else if (!(isAdmin || forceAdmin)) {
            String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                // Public (chưa đăng nhập) => trả job PUBLIC + active + trong khoảng start..end
                if (hasSkills && hasLocations) {
                    page = jobRepository.findPublicJobsBySkillsAndLocations(nameFilter, skillsFilter, locationsFilter, excludeId, pageable);
                } else if (hasSkills) {
                    page = jobRepository.findPublicJobsBySkills(nameFilter, locationFilter, skillsFilter, excludeId, pageable);
                } else if (hasLocations) {
                    page = jobRepository.findPublicJobsByLocations(nameFilter, locationsFilter, excludeId, pageable);
                } else {
                    page = jobRepository.findPublicJobs(nameFilter, locationFilter, excludeId, pageable);
                }
            } else {
                var me = userRepository.findById(currentUserId).orElse(null);
                java.util.Set<String> companyIds = new java.util.LinkedHashSet<>();
                if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) {
                    companyIds.add(me.getCompany().get_id());
                }
                // cộng thêm các công ty mình tạo
                for (com.fullnestjob.modules.companies.entity.Company c : companyRepository.findAllByCreatorId(currentUserId)) {
                    if (c != null && c.get_id() != null) companyIds.add(c.get_id());
                }
                if (!companyIds.isEmpty()) {
                    java.util.List<String> ids = new java.util.ArrayList<>(companyIds);
                    if (nameFilter != null && locationFilter != null) {
                        page = jobRepository.findByCompanyIdsAndNameLikeAndLocationLike(ids, nameFilter, locationFilter, pageable);
                    } else if (nameFilter != null) {
                        page = jobRepository.findByCompanyIdsAndNameLike(ids, nameFilter, pageable);
                    } else if (locationFilter != null) {
                        page = jobRepository.findByCompanyIdsAndLocationLike(ids, locationFilter, pageable);
                    } else {
                        page = jobRepository.findByCompanyIds(ids, pageable);
                    }
                } else {
                    page = Page.empty(pageable);
                }
            }
        } else {
            // Admin scope (role hoặc scope=admin): có thể filter theo level (CSV)
            java.util.List<String> levels = null;
            if (levelCsv != null && !levelCsv.isBlank()) {
                levels = java.util.Arrays.stream(levelCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(String::toUpperCase)
                        .collect(java.util.stream.Collectors.toList());
                if (levels.isEmpty()) levels = null;
            }
            page = jobRepository.findAdminJobs(nameFilter, locationFilter, minSalary, maxSalary, levels, pageable);
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
                boolean desc = key.startsWith("-");
                String raw = desc ? key.substring(1) : key;
                String mapped = raw;
                if ("created".equalsIgnoreCase(raw)) mapped = "createdAt";
                if ("updated".equalsIgnoreCase(raw)) mapped = "updatedAt";
                return desc ? Sort.by(Sort.Order.desc(mapped)) : Sort.by(Sort.Order.asc(mapped));
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
        j.setLevels(body.levels != null ? new java.util.ArrayList<>(body.levels) : new java.util.ArrayList<>());
        j.setDescription(body.description);
        j.setStartDate(body.startDate);
        j.setEndDate(body.endDate);
        j.setIsActive(body.isActive != null ? body.isActive : Boolean.TRUE);
        if (body.company != null && body.company._id != null) {
            Company c = companyRepository.findById(body.company._id).orElse(null);
            j.setCompany(c);
        }
        // Note: Job entity does not expose createdBy setter; skipping audit embed here
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
        if (body.levels != null) j.setLevels(new java.util.ArrayList<>(body.levels));
        if (body.description != null) j.setDescription(body.description);
        if (body.startDate != null) j.setStartDate(body.startDate);
        if (body.endDate != null) j.setEndDate(body.endDate);
        if (body.isActive != null) j.setIsActive(body.isActive);
        if (body.company != null && body.company._id != null) {
            Company c = companyRepository.findById(body.company._id).orElse(null);
            j.setCompany(c);
        }
        // Note: Job entity does not expose updatedBy setter; skipping audit embed here
        return toDetail(jobRepository.save(j));
    }

    @Transactional
    public void delete(String id) {
        java.util.List<com.fullnestjob.modules.resumes.entity.Resume> resumes = resumeRepository.findByJobId(id);
        if (resumes != null && !resumes.isEmpty()) {
            resumeRepository.deleteAll(resumes);
        }
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
        dto.levels = j.getLevels() != null ? new java.util.ArrayList<>(j.getLevels()) : java.util.Collections.emptyList();
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


