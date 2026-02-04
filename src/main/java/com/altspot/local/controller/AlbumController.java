package com.altspot.local.controller;

import com.altspot.local.config.AppConstants;
import com.altspot.local.payload.AlbumDTO;
import com.altspot.local.payload.PageResult;
import com.altspot.local.payload.TrackDTO;
import com.altspot.local.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/albums")
public class AlbumController {


    private final AlbumService albumService;


    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("get/albums")
    public ResponseEntity<PageResult<AlbumDTO>> getAllAlbums(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER , required = false) Integer pageNumber,
            @RequestParam(name = "pageSize" , defaultValue = AppConstants.PAGE_SIZE , required = false) Integer pageSize,
            @RequestParam(name = "sortBy" , defaultValue = AppConstants.SORT_BY_ID , required = false) String sortBy,
            @RequestParam(name = "sortDirection" , defaultValue = AppConstants.SORT_DIR , required = false) String sortDirection
    ) throws IOException {
        PageResult<AlbumDTO> result = albumService.getAlbums(pageNumber , pageSize, sortBy , sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AlbumDTO>> getAlbumsByKeyword(
            @RequestParam(name = "keyword" , required = true) String keyword
    ){
        List<AlbumDTO> result = albumService.getAlbumsByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/artist/{artistId}")
    public ResponseEntity<Set<AlbumDTO>> getAlbumsByArtistId(
            @PathVariable Long artistId
    ){
        Set<AlbumDTO> result = albumService.getAlbumsByArtist(artistId);
        return ResponseEntity.ok(result);
    }




}
