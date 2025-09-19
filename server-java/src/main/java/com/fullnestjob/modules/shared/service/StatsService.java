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
        String companyId = null;
        if (role == null || !(role.equalsIgnoreCase("SUPER_ADMIN"))) {
            // hạn chế theo công ty của user (nếu có)
            if (currentUserId != null) {
                var me = userRepository.findById(currentUserId).orElse(null);
                if (me != null && me.getCompany() != null) companyId = me.getCompany().get_id();
            }
        }

        if (companyId != null) {
            res.put("openJobs", jobRepository.countByCompanyId(companyId));
            res.put("jobsTotal", jobRepository.countByCompanyId(companyId));
            res.put("resumesTotal", resumeRepository.findByCompanyId(companyId).size());
        } else {
            res.put("openJobs", jobRepository.countActivePublicAll());
            res.put("jobsTotal", jobRepository.count());
            res.put("resumesTotal", resumeRepository.count());
        }
        res.put("companiesTotal", companyRepository.count());
        res.put("usersTotal", userRepository.count());
        // map to x/y with readable names
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
        res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySince(from)));
        return res;
    }

    public java.util.Map<String, Object> getTimeSeries(int fromDays) {
        java.util.Date from = java.util.Date.from(java.time.LocalDate.now().minusDays(fromDays).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        String role = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
        String companyId = null;
        if (role == null || !(role.equalsIgnoreCase("SUPER_ADMIN"))) {
            if (currentUserId != null) {
                var me = userRepository.findById(currentUserId).orElse(null);
                if (me != null && me.getCompany() != null) companyId = me.getCompany().get_id();
            }
        }
        if (companyId != null) {
            res.put("jobsPerDay", toPairs(jobRepository.countCreatedPerDaySinceByCompany(from, companyId)));
            res.put("resumesPerDay", toPairs(resumeRepository.countCreatedPerDaySinceByCompany(from, companyId)));
            res.put("jobsByLevels", toPairs(jobRepository.countByLevelsForCompany(companyId)));
        } else {
            res.put("jobsPerDay", toPairs(jobRepository.countCreatedPerDaySince(from)));
            res.put("resumesPerDay", toPairs(resumeRepository.countCreatedPerDaySince(from)));
            res.put("jobsByLevels", toPairs(jobRepository.countByLevels()));
        }
        res.put("companiesPerMonth", toPairs(companyRepository.countCreatedPerMonthSince(from)));
        res.put("usersPerDay", toPairs(userRepository.countCreatedPerDaySince(from)));
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


