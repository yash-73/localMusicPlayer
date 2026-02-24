package com.altspot.local.model;


import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.Objects;


@Entity
@Table(
        name = "playlist_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_playlist_position",
                        columnNames = {"playlist_id", "position"}
                )
        },
        indexes = {
                @Index(name = "idx_playlist_order", columnList = "playlist_id, position"),
                @Index(name = "idx_playlist_item_track", columnList = "track_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "playlist_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_playlist_item_playlist")
    )
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "track_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_playlist_item_track")
    )
    private Track track;

    @Column(nullable = false)
    private Long position;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaylistItem)) return false;
        return id != null && id.equals(((PlaylistItem) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


    // getters / setters
}
