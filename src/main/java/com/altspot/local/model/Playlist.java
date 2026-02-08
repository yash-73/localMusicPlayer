//package com.altspot.local.model;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.time.Instant;
//import java.util.List;
////@Entity
////@Table(
////        name = "playlist",
////        indexes = {
////                @Index(name = "idx_playlist_user", columnList = "user_id")
////        }
////)
//@Getter
//@Setter
//public class Playlist {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "playlist_id")
//    private Long id;
//
//    @Column(name = "playlist_name", nullable = false)
//    private String name;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @OneToMany(
//            mappedBy = "playlist",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    @OrderBy("position ASC")
//    private List<PlaylistItem> playlistItems;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        Instant now = Instant.now();
//        this.createdAt = now;
//        this.updatedAt = now;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = Instant.now();
//    }
//
//    // getters/setters
//}
