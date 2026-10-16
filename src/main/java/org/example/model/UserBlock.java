package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_blocks")
public class UserBlock {

    @EmbeddedId
    private UserBlockId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("blockerId")
    @JoinColumn(name = "blocker_id", nullable = false)
    private AppUser blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("blockedId")
    @JoinColumn(name = "blocked_id", nullable = false)
    private AppUser blocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserBlock() {}

    public UserBlock(AppUser blocker, AppUser blocked) {
        this.id = new UserBlockId(blocker.getId(), blocked.getId());
        this.blocker = blocker;
        this.blocked = blocked;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UserBlockId getId() { return id; }

    public AppUser getBlocker() { return blocker; }
    public AppUser getBlocked() { return blocked; }

    public Instant getCreatedAt() { return createdAt; }
}
