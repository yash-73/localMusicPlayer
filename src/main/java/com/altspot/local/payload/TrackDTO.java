package com.altspot.local.payload;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDTO {
    public Long id;
    public String title;
    public Album album;
    public Set<Artist>  artist;
//    private String genre;
//    public Integer durationSeconds;
//    public Integer sampleRate;
//    private Long fileSize;
}
