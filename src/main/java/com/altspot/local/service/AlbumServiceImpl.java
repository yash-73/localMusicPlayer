package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.payload.*;
import com.altspot.local.repository.AlbumRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;

    public AlbumServiceImpl(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){

        Sort sortByAndOrder = sortDirection.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<AlbumSummary> trackPage = albumRepository.findAllProjectedBy(pageDetails);

        List<AlbumSummary> tracks = trackPage.getContent();

        if (tracks.isEmpty()) throw new GeneralException("No albums available");

        List<AlbumDTO> content = tracks.stream()
                .map(album -> {
                    AlbumDTO albumDTO = new AlbumDTO();
                    albumDTO.setId(album.getId());
                    albumDTO.setName(album.getName());

                    albumDTO.setPrimaryArtistId(album.getPrimaryArtistId());
                    albumDTO.setPrimaryArtistName(album.getPrimaryArtistName());
                    return albumDTO;
                })
                .toList();

        PageResult<AlbumDTO> trackResponse = new PageResult<AlbumDTO>();
        trackResponse.setContent(content);
        trackResponse.setPageNumber(trackPage.getNumber());
        trackResponse.setTotalPages(trackPage.getTotalPages());
        trackResponse.setTotalElements(trackPage.getTotalElements());
        trackResponse.setLastPage(trackPage.isLast());
        trackResponse.setPageSize(trackPage.getSize());

        return trackResponse;
    }
}
