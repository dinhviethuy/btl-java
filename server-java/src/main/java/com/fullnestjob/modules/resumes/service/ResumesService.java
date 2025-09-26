package com.fullnestjob.modules.resumes.service;

import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import com.fullnestjob.modules.jobs.entity.Job;
import com.fullnestjob.modules.jobs.repo.JobRepository;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.CreateResumeBodyDTO;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.JobIdDTO;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.ResumeDetailDTO;
import com.fullnestjob.modules.resumes.dto.ResumeDtos.UpdateResumeBodyDTO;
import com.fullnestjob.modules.resumes.entity.Resume;
import com.fullnestjob.modules.resumes.repo.ResumeRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ResumesService {
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final com.fullnestjob.modules.users.repo.UserRepository userRepository;

    public ResumesService(ResumeRepository resumeRepository, CompanyRepository companyRepository, JobRepository jobRepository, com.fullnestjob.modules.users.repo.UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public PageResultDTO<ResumeDetailDTO> find(PaginationQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        org.springframework.data.domain.Sort sort = parseSort(query);
        var pageable = org.springframework.data.domain.PageRequest.of(current - 1, pageSize, sort);
        String status = query.getStatus();
        Page<Resume> page;
        String companyName = query.getCompanyName();
        String jobName = query.getJobName();
        String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        boolean isAdmin = currentRole != null && (currentRole.equalsIgnoreCase("ADMIN") || currentRole.equalsIgnoreCase("SUPER_ADMIN"));
        if (!isAdmin) {
            String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
            com.fullnestjob.modules.users.entity.User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
            java.util.Set<String> companyIds = new java.util.LinkedHashSet<>();
            if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) {
                companyIds.add(me.getCompany().get_id());
            }
            for (Company c : companyRepository.findAllByCreatorId(currentUserId)) {
                if (c != null && c.get_id() != null) companyIds.add(c.get_id());
            }
            if (!companyIds.isEmpty()) {
                java.util.List<String> ids = new java.util.ArrayList<>(companyIds);
                String companyIdFilter = query.getCompanyId();
                String companyNameFilter = query.getCompanyName();
                String jobNameFilter = query.getJobName();
                if (companyNameFilter != null) companyNameFilter = companyNameFilter.replaceAll("^/+|/i$", "");
                if (jobNameFilter != null) jobNameFilter = jobNameFilter.replaceAll("^/+|/i$", "");
                
                // Xử lý status dạng CSV
                java.util.List<String> statusList = null;
                if (status != null && !status.isBlank()) {
                    String decoded = java.net.URLDecoder.decode(status, java.nio.charset.StandardCharsets.UTF_8);
                    String[] parts = decoded.split(",");
                    statusList = new java.util.ArrayList<>();
                    for (String p : parts) {
                        if (p == null || p.isBlank()) continue;
                        String s = p.replaceAll("^/+|/i$", "").toUpperCase();
                        statusList.add(s);
                    }
                    if (statusList.isEmpty()) statusList = null;
                }
                
                // Kết hợp tất cả các filter
                if (companyIdFilter != null && !companyIdFilter.isBlank() && companyIds.contains(companyIdFilter)) {
                    // Filter theo companyId cụ thể + jobName + status
                    page = resumeRepository.findByCompanyIdAndJobNameAndStatusIn(companyIdFilter, jobNameFilter, statusList, pageable);
                } else {
                    // Filter theo danh sách companyIds + jobName + status
                    page = resumeRepository.findByCompanyIdsAndJobNameAndStatusIn(ids, jobNameFilter, statusList, pageable);
                }
            } else {
                page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
            }
        } else {
            String companyNameFilter = query.getCompanyName();
            String jobNameFilter = query.getJobName();
            String companyIdFilter = query.getCompanyId();
            if (companyNameFilter != null) companyNameFilter = companyNameFilter.replaceAll("^/+|/i$", "");
            if (jobNameFilter != null) jobNameFilter = jobNameFilter.replaceAll("^/+|/i$", "");

            // Xử lý status dạng CSV
            java.util.List<String> statusList = null;
            if (status != null && !status.isBlank()) {
                String decoded = java.net.URLDecoder.decode(status, java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = decoded.split(",");
                statusList = new java.util.ArrayList<>();
                for (String p : parts) {
                    if (p == null || p.isBlank()) continue;
                    String s = p.replaceAll("^/+|/i$", "").toUpperCase();
                    statusList.add(s);
                }
                if (statusList.isEmpty()) statusList = null;
            }

            // Kết hợp tất cả các filter cho admin
            if (companyIdFilter != null && !companyIdFilter.isBlank()) {
                // Filter theo companyId + jobName + status
                page = resumeRepository.findByCompanyIdAndJobNameAndStatusInAdmin(companyIdFilter, jobNameFilter, statusList, pageable);
            } else if (companyNameFilter != null || jobNameFilter != null) {
                // Filter theo companyName + jobName + status
                page = resumeRepository.findByCompanyNameAndJobNameAndStatusInAdmin(companyNameFilter, jobNameFilter, statusList, pageable);
            } else if (statusList != null) {
                // Chỉ filter theo status
                page = resumeRepository.findByStatusInIgnoreCase(statusList, pageable);
            } else {
                // Không có filter nào
                page = resumeRepository.findAll(pageable);
            }
        }
        PageResultDTO<ResumeDetailDTO> res = new PageResultDTO<>();
        res.result = page.getContent().stream().map(this::toDetail).collect(Collectors.toList());
        MetaDTO meta = new MetaDTO();
        meta.current = current;
        meta.pageSize = pageSize;
        meta.total = (int) page.getTotalElements();
        meta.pages = page.getTotalPages();
        res.meta = meta;
        return res;
    }

    private org.springframework.data.domain.Sort parseSort(PaginationQueryDTO query) {
        if (query == null) return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("createdAt"));
        try {
            java.lang.reflect.Field f = query.getClass().getDeclaredField("sort");
            f.setAccessible(true);
            Object v = f.get(query);
            if (v instanceof String s && !s.isBlank()) {
                String key = s.startsWith("sort=") ? s.substring(5) : s;
                if (key.startsWith("-")) return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc(key.substring(1)));
                return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.asc(key));
            }
        } catch (Exception ignored) {}
        return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("createdAt"));
    }

    public ResumeDetailDTO findById(String id) {
        return resumeRepository.findById(id).map(this::toDetail).orElse(null);
    }

    public java.util.List<ResumeDetailDTO> findAllByUserId(String userId) {
        return resumeRepository.findByUserId(userId).stream().map(this::toDetail).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public ResumeDetailDTO create(String userId, String email, CreateResumeBodyDTO body) {
        Company company = companyRepository.findById(body.companyId).orElseThrow();
        Job job = jobRepository.findById(body.jobId).orElseThrow();
        Resume r = new Resume();
        r.setUserId(userId);
        r.setEmail(email);
        r.setUrl(body.url);
        r.setStatus("PENDING");
        r.setCompanyId(company);
        r.setJobId(job);
        return toDetail(resumeRepository.save(r));
    }

    @Transactional
    public ResumeDetailDTO update(String id, String email, String updatedById, UpdateResumeBodyDTO body) {
        Resume r = resumeRepository.findById(id).orElseThrow();
        if (body.status != null) r.setStatus(body.status);
        return toDetail(resumeRepository.save(r));
    }

    @Transactional
    public void delete(String id) {
        resumeRepository.deleteById(id);
    }

    private ResumeDetailDTO toDetail(Resume r) {
        ResumeDetailDTO dto = new ResumeDetailDTO();
        dto._id = r.get_id();
        dto.email = r.getEmail();
        dto.userId = r.getUserId();
        dto.url = r.getUrl();
        dto.status = r.getStatus();
        if (r.getCompanyId() != null) {
            com.fullnestjob.modules.resumes.dto.ResumeDtos.CompanyIdDTO c = new com.fullnestjob.modules.resumes.dto.ResumeDtos.CompanyIdDTO();
            c._id = r.getCompanyId().get_id();
            c.name = r.getCompanyId().getName();
            c.logo = r.getCompanyId().getLogo();
            dto.companyId = c;
        }
        if (r.getJobId() != null) {
            JobIdDTO j = new JobIdDTO();
            j._id = r.getJobId().get_id();
            j.name = r.getJobId().getName();
            dto.jobId = j;
        }
        if (r.getHistory() != null) {
            dto.history = new java.util.ArrayList<>();
            for (var h : r.getHistory()) {
                com.fullnestjob.modules.resumes.dto.ResumeDtos.ResumeHistoryDTO hd = new com.fullnestjob.modules.resumes.dto.ResumeDtos.ResumeHistoryDTO();
                hd.status = h.getStatus();
                hd.updatedAt = h.getUpdatedAt();
                if (h.getUpdatedBy() != null) {
                    com.fullnestjob.modules.resumes.dto.ResumeDtos.ActorDTO a = new com.fullnestjob.modules.resumes.dto.ResumeDtos.ActorDTO();
                    a._id = h.getUpdatedBy().get_id();
                    a.email = h.getUpdatedBy().getEmail();
                    hd.updatedBy = a;
                }
                dto.history.add(hd);
            }
        }
        dto.createdAt = r.getCreatedAt();
        dto.updatedAt = r.getUpdatedAt();
        dto.deletedAt = r.getDeletedAt();
        return dto;
    }
}


