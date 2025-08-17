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

    public JobsService(JobRepository jobRepository, CompanyRepository companyRepository, com.fullnestjob.modules.users.repo.UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
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
        boolean forcePublic = scope != null && scope.equalsIgnoreCase("public");
        if (nameFilter != null) nameFilter = nameFilter.replaceAll("^/+|/i$", "");
        if (locationFilter != null) locationFilter = locationFilter.replaceAll("^/+|/i$", "");

        String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        boolean isAdmin = currentRole != null && (currentRole.equalsIgnoreCase("ADMIN") || currentRole.equalsIgnoreCase("SUPER_ADMIN"));
        if (forcePublic) {
            if (hasSkills && hasLocations) {
                page = jobRepository.findPublicJobsBySkillsAndLocations(nameFilter, skillsFilter, locationsFilter, pageable);
            } else if (hasSkills) {
                page = jobRepository.findPublicJobsBySkills(nameFilter, locationFilter, skillsFilter, pageable);
            } else if (hasLocations) {
                page = jobRepository.findPublicJobsByLocations(nameFilter, locationsFilter, pageable);
            } else {
                page = jobRepository.findPublicJobs(nameFilter, locationFilter, pageable);
            }
        } else if (!isAdmin) {
            String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                // Public (chưa đăng nhập) => trả job PUBLIC + active + trong khoảng start..end
                if (hasSkills && hasLocations) {
                    page = jobRepository.findPublicJobsBySkillsAndLocations(nameFilter, skillsFilter, locationsFilter, pageable);
                } else if (hasSkills) {
                    page = jobRepository.findPublicJobsBySkills(nameFilter, locationFilter, skillsFilter, pageable);
                } else if (hasLocations) {
                    page = jobRepository.findPublicJobsByLocations(nameFilter, locationsFilter, pageable);
                } else {
                    page = jobRepository.findPublicJobs(nameFilter, locationFilter, pageable);
                }
            } else {
                var me = userRepository.findById(currentUserId).orElse(null);
                String companyId = me != null && me.getCompany() != null ? me.getCompany().get_id() : null;
                if (companyId != null) {
                    if (nameFilter != null && locationFilter != null) {
                        page = jobRepository.findByCompanyIdAndNameLikeAndLocationLike(companyId, nameFilter, locationFilter, pageable);
                    } else if (nameFilter != null) {
                        page = jobRepository.findByCompanyIdAndNameLike(companyId, nameFilter, pageable);
                    } else if (locationFilter != null) {
                        page = jobRepository.findByCompanyIdAndLocationLike(companyId, locationFilter, pageable);
                    } else {
                        page = jobRepository.findByCompanyId(companyId, pageable);
                    }
                } else {
                    page = Page.empty(pageable);
                }
            }
        } else {
            if (nameFilter != null && locationFilter != null) {
                page = jobRepository.findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(nameFilter, locationFilter, pageable);
            } else if (nameFilter != null) {
                page = jobRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
            } else if (locationFilter != null) {
                page = jobRepository.findByLocationContainingIgnoreCase(locationFilter, pageable);
            } else {
                page = jobRepository.findAll(pageable);
            }
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


