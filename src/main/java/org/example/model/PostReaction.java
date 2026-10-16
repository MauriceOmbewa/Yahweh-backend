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
        name = "post_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_post_reaction_per_user",
                columnNames = {"post_id", "user_id"}
        )
)
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    /** e.g. AMEN, HEART, FIRE, PRAY */
    @Column(name = "reaction_type", nullable = false, length = 20)
    private String reactionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PostReaction() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- getters & setters ---

    public UUID getId() { return id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public Instant getCreatedAt() { return createdAt; }
}
