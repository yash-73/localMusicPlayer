package com.altspot.local.service;


import com.altspot.local.payload.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Path;

public interface TrackService {

    RescanResult rescan() throws IOException;

    ResponseEntity<Resource> stream(Long trackId, String range) throws IOException;

    PageResult<TrackDTO> getTracks(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException;

    public PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException;
}
