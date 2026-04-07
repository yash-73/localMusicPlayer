package com.altspot.local.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistItemDTO {
    Long playlistId;
    Long playlistItemId;
    Long position;
    Long trackId;
    String trackName;
    Integer durationSeconds;
    Long albumId;
    String albumName;
    Set<ArtistDTO> artists;
}
