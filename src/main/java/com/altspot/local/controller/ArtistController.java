package com.altspot.local.controller;

import com.altspot.local.config.AppConstants;
import com.altspot.local.payload.AlbumDTO;
import com.altspot.local.payload.ArtistDTO;
import com.altspot.local.payload.PageResult;
import com.altspot.local.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("get/artists")
    public ResponseEntity<PageResult<ArtistDTO>> getAllArtists(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER , required = false) Integer pageNumber,
            @RequestParam(name = "pageSize" , defaultValue = AppConstants.PAGE_SIZE , required = false) Integer pageSize,
            @RequestParam(name = "sortBy" , defaultValue = AppConstants.SORT_BY_ID , required = false) String sortBy,
            @RequestParam(name = "sortDirection" , defaultValue = AppConstants.SORT_DIR , required = false) String sortDirection
    ) {
        PageResult<ArtistDTO> result = artistService.getArtists(pageNumber , pageSize, sortBy , sortDirection);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArtistDTO>> getAlbumsByKeyword(
            @RequestParam(name = "keyword" , required = true) String keyword
    ){
        List<ArtistDTO> result = artistService.getArtistsByKeyword(keyword);
        return ResponseEntity.ok(result);
    }
}
