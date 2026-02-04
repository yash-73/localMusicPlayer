package com.altspot.local.service;

import com.altspot.local.payload.AlbumDTO;
import com.altspot.local.payload.PageResult;

import java.util.List;
import java.util.Set;

public interface AlbumService {

    PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);

    List<AlbumDTO> getAlbumsByKeyword(String keyword);

    Set<AlbumDTO> getAlbumsByArtist(Long artistId);
}
