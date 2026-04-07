package com.altspot.local.service;

import com.altspot.local.payload.PlaylistDTO;
import com.altspot.local.payload.PlaylistItemDTO;
import com.altspot.local.payload.PlaylistMeta;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PlaylistService {

    List<PlaylistMeta> getAllPlaylistMetas(Authentication authentication);

    List<PlaylistItemDTO> getAllPlaylistItemsForPlaylist(Long playlistId);

    PlaylistDTO getPlaylistMetaById(Long playlistId);

    PlaylistDTO createNewPlaylist(Authentication authentication , PlaylistMeta playlistMeta);

    PlaylistItemDTO addTrackToPlaylist(Long playlistId, Long trackId);

    PlaylistItemDTO removeTrackFromPlaylist(Long playlistId, Long playlistItemId);

    void reorderPlaylistItem(Long playlistId, Long playlistItemId, Long previousId, Long nextId);


}
