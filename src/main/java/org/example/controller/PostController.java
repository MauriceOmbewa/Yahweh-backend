package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.AddCommentRequest;
import org.example.dto.CreatePostRequest;
import org.example.dto.PagedResponse;
import org.example.dto.PostCommentResponse;
import org.example.dto.PostResponse;
import org.example.dto.ReactToPostRequest;
import org.example.model.Post;
import org.example.model.PostComment;
import org.example.model.PostReaction;
import org.example.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Posts", description = "Share verses, react, comment, and view your friends feed")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // -------------------------------------------------------------------------
    // Posts
    // -------------------------------------------------------------------------

    @Operation(summary = "Share a verse as a post")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Post post = postService.createPost(
                userId,
                request.bibleId(),
                request.verseId(),
                request.verseText(),
                request.caption(),
                request.visibility()
        );
        return enrichedResponse(post, userId);
    }

    @Operation(
            summary = "Get a single post",
            description = "Returns a post with reaction counts and comment count. "
                    + "Friends-only posts are only visible to accepted friends."
    )
    @GetMapping("/{postId}")
    public PostResponse getPost(
            @PathVariable UUID postId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Post post = postService.getPostById(postId, userId);
        return enrichedResponse(post, userId);
    }

    @Operation(
            summary = "Get your friend feed",
            description = "Returns posts from accepted friends and yourself, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping("/feed")
    public PagedResponse<PostResponse> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Page<Post> posts = postService.getFeed(userId, page, size);
        return PagedResponse.from(posts, post -> enrichedResponse(post, userId));
    }

    @Operation(
            summary = "Get posts by a specific user",
            description = "Only accessible for own posts or accepted friends. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping("/user/{targetUserId}")
    public PagedResponse<PostResponse> getUserPosts(
            @PathVariable UUID targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Page<Post> posts = postService.getUserPosts(targetUserId, userId, page, size);
        return PagedResponse.from(posts, post -> enrichedResponse(post, userId));
    }

    @Operation(summary = "Delete one of your posts")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable UUID postId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        postService.deletePost(postId, userId);
    }

    // -------------------------------------------------------------------------
    // Reactions
    // -------------------------------------------------------------------------

    @Operation(
            summary = "React to a post",
            description = "Adds or updates the authenticated user's reaction. "
                    + "Allowed types: AMEN, HEART, FIRE, PRAY."
    )
    @PostMapping("/{postId}/reactions")
    public PostResponse react(
            @PathVariable UUID postId,
            @Valid @RequestBody ReactToPostRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        postService.react(postId, userId, request.reactionType());
        Post post = postService.getPostById(postId, userId);
        return enrichedResponse(post, userId);
    }

    @Operation(summary = "Remove your reaction from a post")
    @DeleteMapping("/{postId}/reactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(
            @PathVariable UUID postId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        postService.removeReaction(postId, userId);
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    @Operation(summary = "Add a comment to a post")
    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentResponse addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        PostComment comment = postService.addComment(postId, userId, request.body());
        return PostCommentResponse.from(comment);
    }

    @Operation(
            summary = "Get comments on a post",
            description = "Returns comments oldest-first. Defaults to page 0, 20 items per page."
    )
    @GetMapping("/{postId}/comments")
    public PagedResponse<PostCommentResponse> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Page<PostComment> comments = postService.getComments(postId, userId, page, size);
        return PagedResponse.from(comments, PostCommentResponse::from);
    }

    @Operation(summary = "Delete one of your comments")
    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        postService.deleteComment(commentId, userId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Builds a PostResponse with live reaction/comment data. */
    private PostResponse enrichedResponse(Post post, UUID viewerId) {
        long commentCount = postService.getCommentCount(post.getId());
        long totalReactions = postService.getTotalReactions(post.getId());
        Map<String, Long> reactionCounts = postService.getReactionCounts(post.getId());
        String viewerReaction = postService.getViewerReaction(post.getId(), viewerId);
        return PostResponse.from(post, commentCount, totalReactions, reactionCounts, viewerReaction);
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
