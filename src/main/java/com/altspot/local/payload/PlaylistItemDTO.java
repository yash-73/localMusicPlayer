package com.altspot.local.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistItemDTO {
    Long playlistItemId;
    Long position;
    Long trackId;
    String trackName;
    Long durationSeconds;
    Long albumId;
    String albumName;
    List<ArtistDTO> artists;
}
