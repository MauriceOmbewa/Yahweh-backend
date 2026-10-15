
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
import java.util.UUID;

@Entity
@Table(
        name = "reading_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_reading_progress_user_chapter",
                        columnNames = {"user_id", "bible_id", "chapter_id"}
                )
        }
)
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "bible_id", nullable = false, length = 100)
    private String bibleId;

    @Column(name = "chapter_id", nullable = false, length = 100)
    private String chapterId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ReadingProgress() {
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (readAt == null) {
            readAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getBibleId() {
        return bibleId;
    }

    public void setBibleId(String bibleId) {
        this.bibleId = bibleId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
