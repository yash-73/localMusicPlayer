package com.altspot.local.controller;

import com.altspot.local.payload.PlaylistDTO;
import com.altspot.local.payload.PlaylistMeta;
import com.altspot.local.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("playlist")
public class PlaylistController {


    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/get/playlists")
    public ResponseEntity<List<PlaylistMeta>> getPlaylistMetas(Authentication authentication) {
        List<PlaylistMeta> result = playlistService.getAllPlaylistMetas(authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("get/playlist/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylistDataById(@RequestParam Long playlistId) {
        PlaylistDTO result = playlistService.getPlaylistMetaById(playlistId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("create")
    public ResponseEntity<PlaylistDTO> createNewPlaylist(@RequestBody PlaylistMeta playlistMeta, Authentication authentication) {
        PlaylistDTO result = playlistService.createNewPlaylist(authentication , playlistMeta);
        return ResponseEntity.ok(result);
    }


}
