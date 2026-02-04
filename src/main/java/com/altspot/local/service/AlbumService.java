package com.altspot.local.service;

import com.altspot.local.payload.AlbumDTO;
import com.altspot.local.payload.PageResult;

public interface AlbumService {

    PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
}
