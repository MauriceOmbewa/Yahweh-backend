package org.example.repository;

import org.example.model.ContentReport;
import org.example.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {

    Page<ContentReport> findAllByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);

    Page<ContentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Prevent duplicate reports from the same reporter on the same post. */
    boolean existsByReporter_IdAndReportedPost_Id(UUID reporterId, UUID postId);

    /** Prevent duplicate user reports from the same reporter. */
    boolean existsByReporter_IdAndReportedUser_Id(UUID reporterId, UUID reportedUserId);
}
