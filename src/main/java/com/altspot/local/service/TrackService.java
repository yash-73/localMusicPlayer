package com.altspot.local.service;


import com.altspot.local.payload.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TrackService {


    ResponseEntity<Resource> stream(Long trackId, String range) throws IOException;
//
    PageResult<TrackDTO> getTracks(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException;

    List<TrackDTO> getTracksByAlbum(Long albumId);

    List<TrackDTO> getTracksByKeyword(String keyword);

    List<TrackDTO> getTracksByArtist(Long artistId);
//
//    PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException;
//
//    PageResult<ArtistDTO> getArtists(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException;
//

}
