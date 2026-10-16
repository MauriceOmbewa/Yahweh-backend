package org.example.repository;

import org.example.model.WeeklyWrapped;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyWrappedRepository extends JpaRepository<WeeklyWrapped, UUID> {

    /** Most recent wrapped card for a user, regardless of week. */
    Optional<WeeklyWrapped> findFirstByUser_IdOrderByWeekStartDesc(UUID userId);

    /** All wrapped cards for a user, newest first. */
    List<WeeklyWrapped> findAllByUser_IdOrderByWeekStartDesc(UUID userId);

    /** Lookup by exact week (used to avoid generating duplicates). */
    Optional<WeeklyWrapped> findByUser_IdAndWeekStart(UUID userId, LocalDate weekStart);

    /** Check whether a card already exists for a given user + week. */
    boolean existsByUser_IdAndWeekStart(UUID userId, LocalDate weekStart);
}
