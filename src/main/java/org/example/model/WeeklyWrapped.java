package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "weekly_wrapped",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_weekly_wrapped_user_week",
                columnNames = {"user_id", "week_start"}
        )
)
public class WeeklyWrapped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(name = "chapters_read", nullable = false)
    private int chaptersRead = 0;

    @Column(name = "total_reading_seconds", nullable = false)
    private long totalReadingSeconds = 0;

    @Column(name = "reading_days", nullable = false)
    private int readingDays = 0;

    @Column(name = "longest_session_seconds", nullable = false)
    private long longestSessionSeconds = 0;

    /** 0–23 local hour with the most reading time; null if no sessions. */
    @Column(name = "peak_reading_hour")
    private Integer peakReadingHour;

    @Column(name = "top_bible_id", length = 100)
    private String topBibleId;

    @Column(name = "top_book_id", length = 50)
    private String topBookId;

    /** Milestone badge earned this week, e.g. FIRST_WEEK, SEVEN_DAY_STREAK. Null = none. */
    @Column(name = "milestone", length = 50)
    private String milestone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WeeklyWrapped() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- getters & setters ---

    public UUID getId() { return id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public LocalDate getWeekEnd() { return weekEnd; }
    public void setWeekEnd(LocalDate weekEnd) { this.weekEnd = weekEnd; }

    public int getChaptersRead() { return chaptersRead; }
    public void setChaptersRead(int chaptersRead) { this.chaptersRead = chaptersRead; }

    public long getTotalReadingSeconds() { return totalReadingSeconds; }
    public void setTotalReadingSeconds(long totalReadingSeconds) { this.totalReadingSeconds = totalReadingSeconds; }

    public int getReadingDays() { return readingDays; }
    public void setReadingDays(int readingDays) { this.readingDays = readingDays; }

    public long getLongestSessionSeconds() { return longestSessionSeconds; }
    public void setLongestSessionSeconds(long longestSessionSeconds) { this.longestSessionSeconds = longestSessionSeconds; }

    public Integer getPeakReadingHour() { return peakReadingHour; }
    public void setPeakReadingHour(Integer peakReadingHour) { this.peakReadingHour = peakReadingHour; }

    public String getTopBibleId() { return topBibleId; }
    public void setTopBibleId(String topBibleId) { this.topBibleId = topBibleId; }

    public String getTopBookId() { return topBookId; }
    public void setTopBookId(String topBookId) { this.topBookId = topBookId; }

    public String getMilestone() { return milestone; }
    public void setMilestone(String milestone) { this.milestone = milestone; }

    public Instant getCreatedAt() { return createdAt; }
}
