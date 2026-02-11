package com.altspot.local.controller;

import com.altspot.local.config.AppConstants;
import com.altspot.local.payload.*;
import com.altspot.local.service.TrackService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamAudio(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) throws IOException {
        return trackService.stream(id, range);
    }

    @GetMapping("/get/track/{id}")
    public ResponseEntity<TrackDTO> getTrack(@PathVariable Long id){
        TrackDTO track = trackService.getTrackById(id);
        return  ResponseEntity.ok(track);   
    }

    @GetMapping("get/tracks")
    public ResponseEntity<PageResult<TrackDTO>> getAllTracks(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER , required = false) Integer pageNumber,
            @RequestParam(name = "pageSize" , defaultValue = AppConstants.PAGE_SIZE , required = false) Integer pageSize,
            @RequestParam(name = "sortBy" , defaultValue = AppConstants.SORT_BY_ID , required = false) String sortBy,
            @RequestParam(name = "sortDirection" , defaultValue = AppConstants.SORT_DIR , required = false) String sortDirection
    ) throws IOException {
        PageResult<TrackDTO> result = trackService.getTracks(pageNumber , pageSize, sortBy , sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("get/album/{albumId}")
    public ResponseEntity<List<TrackDTO>> getTracksByAlbum(
            @PathVariable Long albumId
    ){
        List<TrackDTO> result = trackService.getTracksByAlbum(albumId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrackDTO>> getTracksByKeyword(
            @RequestParam(name = "keyword" , required = true) String keyword
    ){
        List<TrackDTO> result = trackService.getTracksByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/artist/{artistId}")
    public ResponseEntity<List<TrackDTO>> getTracksByArtistId(
            @PathVariable Long artistId
    ){
        List<TrackDTO> result = trackService.getTracksByArtist(artistId);
        return ResponseEntity.ok(result);
    }






}
