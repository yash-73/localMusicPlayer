package com.altspot.local.service;

import com.altspot.local.payload.ArtistDTO;
import com.altspot.local.payload.PageResult;

import java.util.List;

public interface ArtistService {
    PageResult<ArtistDTO> getArtists(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    List<ArtistDTO> getArtistsByKeyword(String keyword);
    ArtistDTO getArtistById(Long artistId);
}
