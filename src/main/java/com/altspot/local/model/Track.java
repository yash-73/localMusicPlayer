package com.altspot.local.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "track",
        uniqueConstraints = @UniqueConstraint(columnNames = "file_path")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id")
    private Long id;

    @Column(name = "track_name")
    private String name;

    /* ---------- Album relationship ---------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(name = "album_position")
    private Integer albumPosition;

    /* ---------- Artist relationship ---------- */
    @ManyToMany
    @JoinTable(
            name = "track_artist",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

    /* ---------- Metadata ---------- */
    private String genre;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "sample_rate")
    private Integer sampleRate;

    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "last_scanned_at")
    private Instant lastScannedAt;

    /* ---------- Equality based on natural key ---------- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track)) return false;
        Track track = (Track) o;
        return Objects.equals(filePath, track.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }
}
