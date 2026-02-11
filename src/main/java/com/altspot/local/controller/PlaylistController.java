package com.altspot.local.controller;

import com.altspot.local.payload.PlaylistMeta;
import com.altspot.local.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("playlist")
public class PlaylistController {


    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/get/playlists")
    public ResponseEntity<List<PlaylistMeta>> getPlaylistMeta(Authentication authentication) {
        List<PlaylistMeta> result = playlistService.getAllPlaylistMetas(authentication);
        return ResponseEntity.ok(result);
    }

//    public ResponseEntity<> getPlaylistMetas(Authentication authentication) {/}

}
