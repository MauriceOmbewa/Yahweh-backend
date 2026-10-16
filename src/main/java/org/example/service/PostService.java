package org.example.service;

import org.example.model.AppUser;
import org.example.model.NotificationType;
import org.example.model.Post;
import org.example.model.PostComment;
import org.example.model.PostReaction;
import org.example.model.PostVisibility;
import org.example.repository.AppUserRepository;
import org.example.repository.FriendshipRepository;
import org.example.repository.PostCommentRepository;
import org.example.repository.PostReactionRepository;
import org.example.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class PostService {

    private static final Set<String> ALLOWED_REACTIONS =
            Set.of("AMEN", "HEART", "FIRE", "PRAY");

    private final PostRepository postRepository;
    private final PostReactionRepository reactionRepository;
    private final PostCommentRepository commentRepository;
    private final AppUserRepository appUserRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;
    private final UserBlockService userBlockService;

    public PostService(
            PostRepository postRepository,
            PostReactionRepository reactionRepository,
            PostCommentRepository commentRepository,
            AppUserRepository appUserRepository,
            FriendshipRepository friendshipRepository,
            NotificationService notificationService,
            UserBlockService userBlockService
    ) {
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.appUserRepository = appUserRepository;
        this.friendshipRepository = friendshipRepository;
        this.notificationService = notificationService;
        this.userBlockService = userBlockService;
    }

    // -------------------------------------------------------------------------
    // Posts
    // -------------------------------------------------------------------------

    @Transactional
    public Post createPost(
            UUID userId,
            String bibleId,
            String verseId,
            String verseText,
            String caption,
            String visibilityRaw
    ) {
        AppUser user = findUser(userId);

        PostVisibility visibility = parseVisibility(visibilityRaw);

        Post post = new Post();
        post.setUser(user);
        post.setBibleId(bibleId.trim());
        post.setVerseId(verseId.trim());
        post.setVerseText(verseText != null ? verseText.trim() : null);
        post.setCaption(caption != null ? caption.trim() : null);
        post.setVisibility(visibility);

        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = getPost(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own posts.");
        }
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Post getPostById(UUID postId, UUID viewerId) {
        Post post = getPost(postId);
        assertCanView(post, viewerId);
        return post;
    }

    /** Paginated friend feed: posts from friends + own posts, newest first, blocking enforced. */
    @Transactional(readOnly = true)
    public Page<Post> getFeed(UUID userId, int page, int size) {
        List<UUID> friendIds = friendshipRepository.findFriendIds(userId);
        // Remove any friends that have been blocked in either direction.
        Set<UUID> blockedIds = userBlockService.getBlockedUserIds(userId);
        List<UUID> visibleFriendIds = friendIds.stream()
                .filter(id -> !blockedIds.contains(id))
                .toList();
        return postRepository.findFeedForUser(userId, visibleFriendIds, PageRequest.of(page, size));
    }

    /** All posts by a specific user (only own posts or accepted friends). */
    @Transactional(readOnly = true)
    public Page<Post> getUserPosts(UUID targetUserId, UUID viewerId, int page, int size) {
        if (!targetUserId.equals(viewerId)) {
            boolean areFriends = friendshipRepository.findByUserPair(viewerId, targetUserId)
                    .map(f -> f.getStatus().name().equals("ACCEPTED"))
                    .orElse(false);
            if (!areFriends) {
                throw new IllegalArgumentException(
                        "You can only view posts from your friends.");
            }
        }
        return postRepository.findAllByUser_IdOrderByCreatedAtDesc(targetUserId, PageRequest.of(page, size));
    }

    // -------------------------------------------------------------------------
    // Reactions
    // -------------------------------------------------------------------------

    @Transactional
    public PostReaction react(UUID postId, UUID userId, String reactionType) {
        String normalizedType = reactionType.trim().toUpperCase();
        if (!ALLOWED_REACTIONS.contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Invalid reaction type. Allowed: " + ALLOWED_REACTIONS);
        }

        Post post = getPost(postId);
        assertCanView(post, userId);

        // Upsert: update existing reaction or create new one.
        PostReaction reaction = reactionRepository
                .findByPost_IdAndUser_Id(postId, userId)
                .orElseGet(() -> {
                    PostReaction r = new PostReaction();
                    r.setPost(post);
                    r.setUser(findUser(userId));
                    return r;
                });

        reaction.setReactionType(normalizedType);
        PostReaction saved = reactionRepository.save(reaction);

        // Notify the post author (skip if reacting to own post).
        notificationService.dispatch(
                post.getUser().getId(), NotificationType.POST_REACTION,
                userId, post.getId());

        return saved;
    }

    @Transactional
    public void removeReaction(UUID postId, UUID userId) {
        if (!reactionRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            throw new IllegalArgumentException("No reaction found to remove.");
        }
        reactionRepository.deleteByPost_IdAndUser_Id(postId, userId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getReactionCounts(UUID postId) {
        return reactionRepository.countByTypeForPost(postId)
                .stream()
                .collect(Collectors.toMap(
                        PostReactionRepository.ReactionCount::getReactionType,
                        PostReactionRepository.ReactionCount::getCount
                ));
    }

    @Transactional(readOnly = true)
    public String getViewerReaction(UUID postId, UUID userId) {
        return reactionRepository.findByPost_IdAndUser_Id(postId, userId)
                .map(PostReaction::getReactionType)
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    @Transactional
    public PostComment addComment(UUID postId, UUID userId, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Comment body must not be blank.");
        }
        if (body.length() > 1000) {
            throw new IllegalArgumentException("Comment must not exceed 1000 characters.");
        }

        Post post = getPost(postId);
        assertCanView(post, userId);

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(findUser(userId));
        comment.setBody(body.trim());
        PostComment saved = commentRepository.save(comment);

        // Notify the post author (skip if commenting on own post).
        notificationService.dispatch(
                post.getUser().getId(), NotificationType.POST_COMMENT,
                userId, post.getId());

        return saved;
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        PostComment comment = commentRepository.findByIdAndUser_Id(commentId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Comment not found or you do not own it."));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Page<PostComment> getComments(UUID postId, UUID viewerId, int page, int size) {
        Post post = getPost(postId);
        assertCanView(post, viewerId);
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(
                postId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getCommentCount(UUID postId) {
        return commentRepository.countByPost_Id(postId);
    }

    @Transactional(readOnly = true)
    public long getTotalReactions(UUID postId) {
        return reactionRepository.countByPost_Id(postId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Post getPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
    }

    private AppUser findUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    /**
     * A viewer can see a post if:
     * - they are the author, OR
     * - the post is PUBLIC, OR
     * - the post is FRIENDS_ONLY and the viewer is an accepted friend
     */
    private void assertCanView(Post post, UUID viewerId) {
        if (post.getUser().getId().equals(viewerId)) return;
        if (post.getVisibility() == PostVisibility.PUBLIC) return;

        boolean areFriends = friendshipRepository
                .findByUserPair(viewerId, post.getUser().getId())
                .map(f -> f.getStatus().name().equals("ACCEPTED"))
                .orElse(false);

        if (!areFriends) {
            throw new IllegalArgumentException("Post not found.");
        }
    }

    private PostVisibility parseVisibility(String raw) {
        if (raw == null || raw.isBlank()) return PostVisibility.FRIENDS_ONLY;
        try {
            return PostVisibility.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid visibility. Allowed values: PUBLIC, FRIENDS_ONLY.");
        }
    }
}
