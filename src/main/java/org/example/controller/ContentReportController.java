package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.ContentReportResponse;
import org.example.dto.PagedResponse;
import org.example.dto.ReportRequest;
import org.example.model.ContentReport;
import org.example.service.ContentReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Moderation", description = "Report posts and users; admin review queue")
@RestController
@RequestMapping("/api/reports")
public class ContentReportController {

    private final ContentReportService contentReportService;

    public ContentReportController(ContentReportService contentReportService) {
        this.contentReportService = contentReportService;
    }

    // -------------------------------------------------------------------------
    // User-facing report submission
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Report a post",
            description = "Submits a moderation report for the specified post. "
                    + "Valid reasons: SPAM, INAPPROPRIATE, HARASSMENT, HATE_SPEECH, MISINFORMATION, OTHER."
    )
    @PostMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentReportResponse reportPost(
            @PathVariable UUID postId,
            @Valid @RequestBody ReportRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        ContentReport report = contentReportService.reportPost(
                userId, postId, request.reason(), request.description());
        return ContentReportResponse.from(report);
    }

    @Operation(
            summary = "Report a user",
            description = "Submits a moderation report for the specified user account. "
                    + "Valid reasons: SPAM, INAPPROPRIATE, HARASSMENT, HATE_SPEECH, MISINFORMATION, OTHER."
    )
    @PostMapping("/users/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentReportResponse reportUser(
            @PathVariable UUID targetUserId,
            @Valid @RequestBody ReportRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        ContentReport report = contentReportService.reportUser(
                userId, targetUserId, request.reason(), request.description());
        return ContentReportResponse.from(report);
    }

    // -------------------------------------------------------------------------
    // Admin / moderation queue
    // -------------------------------------------------------------------------

    @Operation(
            summary = "[Admin] Get pending reports",
            description = "Returns all reports with status PENDING, oldest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping("/pending")
    public PagedResponse<ContentReportResponse> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Page<ContentReport> reports = contentReportService.getPendingReports(page, size);
        return PagedResponse.from(reports, ContentReportResponse::from);
    }

    @Operation(
            summary = "[Admin] Get all reports",
            description = "Returns all reports regardless of status, newest first."
    )
    @GetMapping
    public PagedResponse<ContentReportResponse> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Page<ContentReport> reports = contentReportService.getAllReports(page, size);
        return PagedResponse.from(reports, ContentReportResponse::from);
    }

    @Operation(
            summary = "[Admin] Review a report",
            description = "Sets the report status to REVIEWED or DISMISSED. "
                    + "Records the reviewer and timestamp."
    )
    @PutMapping("/{reportId}/review")
    public ContentReportResponse reviewReport(
            @PathVariable UUID reportId,
            @RequestParam String status,
            Authentication authentication
    ) {
        UUID reviewerId = getUserId(authentication);
        ContentReport report = contentReportService.reviewReport(reportId, reviewerId, status);
        return ContentReportResponse.from(report);
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
