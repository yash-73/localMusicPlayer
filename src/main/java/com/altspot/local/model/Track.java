package com.altspot.local.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "tracks" ,  uniqueConstraints = {@UniqueConstraint(columnNames = "file_path")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String album;
    private String artist;
    private String genre;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "sample_rate")
    private Integer sampleRate;

    @Column(name = "file_path" , nullable = false , unique = true)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "last_scanned_at")
    private Instant lastScannedAt;

    @Override
    public int hashCode(){
        return Objects.hash(filePath);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(filePath, track.filePath);
    }


}
