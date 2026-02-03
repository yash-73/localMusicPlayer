package com.altspot.local.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "album")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long id;

    @Column(name = "album_name", nullable = false)
    private String name;

    /* ---------- Identity artist ---------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_artist_id")
    private Artist primaryArtist;


    /* ---------- Contributors ---------- */
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "album_artist",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

    /* ---------- Equality ---------- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return Objects.equals(name, album.name) &&
                Objects.equals(
                        primaryArtist != null ? primaryArtist.getId() : null,
                        album.primaryArtist != null ? album.primaryArtist.getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                primaryArtist != null ? primaryArtist.getId() : null
        );
    }
}
