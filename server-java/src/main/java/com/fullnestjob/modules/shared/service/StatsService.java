package com.fullnestjob.modules.shared.service;

import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final com.fullnestjob.modules.jobs.repo.JobRepository jobRepository;
    private final com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository;
    private final com.fullnestjob.modules.companies.repo.CompanyRepository companyRepository;
    private final com.fullnestjob.modules.users.repo.UserRepository userRepository;

    public StatsService(com.fullnestjob.modules.jobs.repo.JobRepository jobRepository,
                        com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository,
                        com.fullnestjob.modules.companies.repo.CompanyRepository companyRepository,
                        com.fullnestjob.modules.users.repo.UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public java.util.Map<String, Object> getOverview(int fromDays) {
        java.util.Date from = java.util.Date.from(java.time.LocalDate.now().minusDays(fromDays).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        String role = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
        boolean isSuperAdmin = (role != null && role.equalsIgnoreCase("SUPER_ADMIN"));
        java.util.List<String> companyIds = null;
        if (!isSuperAdmin) {
            java.util.Set<String> ids = new java.util.LinkedHashSet<>();
            var me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
            if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) ids.add(me.getCompany().get_id());
            for (com.fullnestjob.modules.companies.entity.Company c : companyRepository.findAllByCreatorId(currentUserId)) {
                if (c != null && c.get_id() != null) ids.add(c.get_id());
            }
            companyIds = new java.util.ArrayList<>(ids);
        }

        if (!isSuperAdmin && (companyIds == null || companyIds.isEmpty())) {
            // Non-super-admin nhưng không có company nào trong phạm vi => tất cả thống kê = 0
            res.put("openJobs", 0L);
            res.put("jobsTotal", 0L);
            res.put("resumesTotal", 0L);
            res.put("companiesTotal", 0L);
            res.put("usersTotal", 0L);
            res.put("resumesByStatus", java.util.List.of());
            res.put("usersPerDay", java.util.List.of());
            return res;
        }

        if (!isSuperAdmin) {
            res.put("openJobs", jobRepository.countActivePublicByCompanyIds(companyIds));
            res.put("jobsTotal", jobRepository.countByCompanyIdIn(companyIds));
            res.put("resumesTotal", resumeRepository.countByCompanyIds(companyIds));
            // usersTotal trong scope công ty
            res.put("usersTotal", userRepository.countByCompanyIds(companyIds));
            // resumes by status trong scope
            java.util.List<Object[]> rows = resumeRepository.countByStatusForCompanies(companyIds);
            java.util.List<java.util.Map<String, Object>> byStatus = new java.util.ArrayList<>();
            java.util.Map<String, String> labelMap = new java.util.HashMap<>();
            labelMap.put("PENDING", "PENDING");
            labelMap.put("REVIEWING", "REVIEWING");
            labelMap.put("APPROVED", "APPROVED");
            labelMap.put("REJECTED", "REJECTED");
            for (Object[] r : rows) {
                String key = String.valueOf(r[0]);
                String label = labelMap.getOrDefault(key, key);
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("x", label);
                m.put("y", ((Number) r[1]).longValue());
                byStatus.add(m);
            }
            res.put("resumesByStatus", byStatus);
            res.put("companiesTotal", (long) companyIds.size());
        } else {
            res.put("openJobs", jobRepository.countActivePublicAll());
            res.put("jobsTotal", jobRepository.count());
            res.put("resumesTotal", resumeRepository.count());
            res.put("usersTotal", userRepository.count());
            // resumesByStatus toàn hệ thống
            java.util.List<Object[]> rows = resumeRepository.countByStatus();
            java.util.List<java.util.Map<String, Object>> byStatus = new java.util.ArrayList<>();
            java.util.Map<String, String> labelMap = new java.util.HashMap<>();
            labelMap.put("PENDING", "PENDING");
            labelMap.put("REVIEWING", "REVIEWING");
            labelMap.put("APPROVED", "APPROVED");
            labelMap.put("REJECTED", "REJECTED");
            for (Object[] r : rows) {
                String key = String.valueOf(r[0]);
                String label = labelMap.getOrDefault(key, key);
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("x", label);
                m.put("y", ((Number) r[1]).longValue());
                byStatus.add(m);
            }
            res.put("resumesByStatus", byStatus);
            res.put("companiesTotal", companyRepository.count());
        }
        // usersPerDay: hạn chế theo scope nếu cần
        if (!isSuperAdmin) {
            res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySinceByCompanyIds(from, companyIds)));
        } else {
            res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySince(from)));
        }
        return res;
    }

    public java.util.Map<String, Object> getTimeSeries(int fromDays) {
        java.util.Date from = java.util.Date.from(java.time.LocalDate.now().minusDays(fromDays).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        String role = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
        boolean isSuperAdmin2 = (role != null && role.equalsIgnoreCase("SUPER_ADMIN"));
        java.util.List<String> companyIds2 = null;
        if (!isSuperAdmin2) {
            java.util.Set<String> ids = new java.util.LinkedHashSet<>();
            var me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
            if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) ids.add(me.getCompany().get_id());
            for (com.fullnestjob.modules.companies.entity.Company c : companyRepository.findAllByCreatorId(currentUserId)) {
                if (c != null && c.get_id() != null) ids.add(c.get_id());
            }
            companyIds2 = new java.util.ArrayList<>(ids);
        }

        if (!isSuperAdmin2 && (companyIds2 == null || companyIds2.isEmpty())) {
            res.put("jobsPerDay", java.util.List.of());
            res.put("resumesPerDay", java.util.List.of());
            res.put("jobsByLevels", java.util.List.of());
            res.put("companiesPerDay", java.util.List.of());
            res.put("usersPerDay", java.util.List.of());
            return res;
        }

        if (!isSuperAdmin2) {
            res.put("jobsPerDay", toPairs(jobRepository.countCreatedPerDaySinceByCompanyIds(from, companyIds2)));
            res.put("resumesPerDay", toPairs(resumeRepository.countCreatedPerDaySinceByCompanyIds(from, companyIds2)));
            res.put("jobsByLevels", toPairs(jobRepository.countByLevelsForCompanies(companyIds2)));
            res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySinceByCompanyIds(from, companyIds2)));
        } else {
            res.put("jobsPerDay", toPairs(jobRepository.countCreatedPerDaySince(from)));
            res.put("resumesPerDay", toPairs(resumeRepository.countCreatedPerDaySince(from)));
            res.put("jobsByLevels", toPairs(jobRepository.countByLevels()));
            res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySince(from)));
        }
        res.put("companiesPerDay", toPairs(companyRepository.countCreatedPerDaySince(from)));
        return res;
    }

    public java.util.List<java.util.Map<String, Object>> getTopCompanies(int limit) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (Object[] row : jobRepository.topCompaniesByOpenJobs(limit)) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", row[0]);
            m.put("name", row[1]);
            m.put("logo", row[2]);
            m.put("count", ((Number) row[3]).longValue());
            list.add(m);
        }
        return list;
    }

    private java.util.List<java.util.Map<String, Object>> toPairs(java.util.List<Object[]> rows) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        if (rows == null) return list;
        for (Object[] r : rows) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("x", String.valueOf(r[0]));
            m.put("y", ((Number) r[1]).longValue());
            list.add(m);
        }
        return list;
    }
}


