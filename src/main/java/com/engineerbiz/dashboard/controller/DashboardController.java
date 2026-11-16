package com.engineerbiz.dashboard.controller;

import com.engineerbiz.common.response.ApiResponse;
import com.engineerbiz.dashboard.dto.DashboardSummaryResponse;
import com.engineerbiz.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Executive Dashboard & KPIs", description = "Real-Time Company Analytics, Financial Health, and Operational Metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    @Operation(summary = "Get high-level company executive dashboard metrics")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
