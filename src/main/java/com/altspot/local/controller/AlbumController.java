package com.altspot.local.controller;

import com.altspot.local.config.AppConstants;
import com.altspot.local.payload.AlbumDTO;
import com.altspot.local.payload.PageResult;
import com.altspot.local.payload.TrackDTO;
import com.altspot.local.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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


}
