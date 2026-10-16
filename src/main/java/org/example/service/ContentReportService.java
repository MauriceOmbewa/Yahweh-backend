package org.example.service;

import org.example.model.AppUser;
import org.example.model.ContentReport;
import org.example.model.Post;
import org.example.model.ReportReason;
import org.example.model.ReportStatus;
import org.example.repository.AppUserRepository;
import org.example.repository.ContentReportRepository;
import org.example.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ContentReportService {

    private final ContentReportRepository reportRepository;
    private final AppUserRepository appUserRepository;
    private final PostRepository postRepository;

    public ContentReportService(
            ContentReportRepository reportRepository,
            AppUserRepository appUserRepository,
            PostRepository postRepository
    ) {
        this.reportRepository = reportRepository;
        this.appUserRepository = appUserRepository;
        this.postRepository = postRepository;
    }

    // -------------------------------------------------------------------------
    // Submit reports
    // -------------------------------------------------------------------------

    @Transactional
    public ContentReport reportPost(
            UUID reporterId,
            UUID postId,
            String reasonRaw,
            String description
    ) {
        AppUser reporter = findUser(reporterId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));

        if (post.getUser().getId().equals(reporterId)) {
            throw new IllegalArgumentException("You cannot report your own post.");
        }
        if (reportRepository.existsByReporter_IdAndReportedPost_Id(reporterId, postId)) {
            throw new IllegalArgumentException("You have already reported this post.");
        }

        ContentReport report = new ContentReport();
        report.setReporter(reporter);
        report.setReportedPost(post);
        report.setReason(parseReason(reasonRaw));
        report.setDescription(description != null ? description.trim() : null);

        return reportRepository.save(report);
    }

    @Transactional
    public ContentReport reportUser(
            UUID reporterId,
            UUID targetUserId,
            String reasonRaw,
            String description
    ) {
        if (reporterId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot report yourself.");
        }

        AppUser reporter = findUser(reporterId);
        AppUser target = findUser(targetUserId);

        if (reportRepository.existsByReporter_IdAndReportedUser_Id(reporterId, targetUserId)) {
            throw new IllegalArgumentException("You have already reported this user.");
        }

        ContentReport report = new ContentReport();
        report.setReporter(reporter);
        report.setReportedUser(target);
        report.setReason(parseReason(reasonRaw));
        report.setDescription(description != null ? description.trim() : null);

        return reportRepository.save(report);
    }

    // -------------------------------------------------------------------------
    // Admin / moderation
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ContentReport> getPendingReports(int page, int size) {
        return reportRepository.findAllByStatusOrderByCreatedAtAsc(
                ReportStatus.PENDING, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<ContentReport> getAllReports(int page, int size) {
        return reportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Transactional
    public ContentReport reviewReport(
            UUID reportId,
            UUID reviewerId,
            String newStatusRaw
    ) {
        ContentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalArgumentException("This report has already been reviewed.");
        }

        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(newStatusRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed: REVIEWED, DISMISSED.");
        }

        if (newStatus == ReportStatus.PENDING) {
            throw new IllegalArgumentException("Cannot set status back to PENDING.");
        }

        AppUser reviewer = findUser(reviewerId);
        report.setStatus(newStatus);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(Instant.now());

        return reportRepository.save(report);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AppUser findUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private ReportReason parseReason(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Report reason is required.");
        }
        try {
            return ReportReason.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid reason. Allowed: SPAM, INAPPROPRIATE, HARASSMENT, "
                    + "HATE_SPEECH, MISINFORMATION, OTHER.");
        }
    }
}
