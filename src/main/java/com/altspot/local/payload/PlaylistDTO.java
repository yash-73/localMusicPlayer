package com.altspot.local.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDTO {
    Long playlistId;
    String playlistName;
    List<PlaylistItemDTO> playlistItemDTOList;

    public PlaylistDTO(Long playlistId, String playlistName) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
    }
}
