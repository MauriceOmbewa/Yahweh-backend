package org.example.model;

public enum NotificationType {
    FRIEND_REQUEST,        // someone sent you a friend request
    FRIEND_REQUEST_ACCEPTED, // your request was accepted
    POST_REACTION,         // someone reacted to your post
    POST_COMMENT,          // someone commented on your post
    COMMENT_REPLY          // reserved for future reply threads
}
