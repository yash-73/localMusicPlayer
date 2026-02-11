package com.altspot.local.service;

import com.altspot.local.payload.PlaylistItemDTO;
import com.altspot.local.payload.PlaylistMeta;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PlaylistService {

    List<PlaylistMeta> getAllPlaylistMetas(Authentication authentication);

    List<PlaylistItemDTO> getAllPlaylistItemsForPlaylist(Long playlistId);

}
