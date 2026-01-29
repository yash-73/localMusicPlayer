package com.altspot.local.service;


import com.altspot.local.payload.PageResult;
import com.altspot.local.payload.RescanResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Path;

public interface TrackService {

    RescanResult rescan() throws IOException;

    ResponseEntity<Resource> stream(Long trackId, String range) throws IOException;

    PageResult getTracks(Integer pageNumber, Integer pageSize, String sortDirection) throws IOException;
}
