package com.fullnestjob.modules.shared.controller;

import com.fullnestjob.common.response.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stats")
public class StatsController {

    private final com.fullnestjob.modules.shared.service.StatsService statsService;

    public StatsController(com.fullnestjob.modules.shared.service.StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    @Message("Overview fetched successfully")
    public ResponseEntity<java.util.Map<String, Object>> overview(@RequestParam(value = "fromDays", required = false, defaultValue = "30") int fromDays) {
        // Cho phép user bình thường truy cập tổng quan
        return ResponseEntity.ok(statsService.getOverview(fromDays));
    }

    @GetMapping("/timeseries")
    @Message("Time series fetched successfully")
    public ResponseEntity<java.util.Map<String, Object>> timeSeries(@RequestParam(value = "fromDays", required = false, defaultValue = "30") int fromDays) {
        // Cho phép user bình thường truy cập chuỗi thời gian
        return ResponseEntity.ok(statsService.getTimeSeries(fromDays));
    }

    @GetMapping("/top-companies")
    @Message("Top companies fetched successfully")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> topCompanies(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        enforceAdminAccess();
        return ResponseEntity.ok(statsService.getTopCompanies(limit));
    }

    private void enforceAdminAccess() {
        String role = com.fullnestjob.security.SecurityUtils.getCurrentRole();
        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("SUPER_ADMIN"))) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
    }
}


