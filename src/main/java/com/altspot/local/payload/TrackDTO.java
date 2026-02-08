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
    public String name;
    public Integer durationSeconds;
    public String albumName;
    public Set<ArtistDTO> artists;
}
