package com.altspot.local.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumTrackDTO {
    public Long id;
    public String name;
    public Integer durationSeconds;
    public String albumName;
    public Long albumId;
    public Integer albumPosition;
    public Set<ArtistDTO> artists;
}
