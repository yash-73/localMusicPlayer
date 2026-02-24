package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import com.altspot.local.payload.*;
import com.altspot.local.repository.AlbumRepository;
import com.altspot.local.repository.ArtistRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final ModelMapper modelMapper;
    private final AlbumRepository albumRepository;

    private final ArtistRepository artistRepository;

    public AlbumServiceImpl(ModelMapper modelMapper, AlbumRepository albumRepository,  ArtistRepository artistRepository) {
        this.modelMapper = modelMapper;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
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
                    albumDTO.setAlbumArtUrl(album.getAlbumArtPath());
                    albumDTO.setReleaseYear(album.getReleaseYear());

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

    @Override
    public List<AlbumDTO> getAlbumsByKeyword(String keyword) {
        String normalizedKeyword = keyword == null
                ? ""
                : keyword.trim().toLowerCase();

        if(normalizedKeyword.isEmpty()) throw new GeneralException("Keyword is empty");

        List<AlbumSummary> albumSummaries = albumRepository.searchByPrefix(normalizedKeyword);
        List<AlbumDTO> albums =  albumSummaries.stream().map(album -> {
            AlbumDTO albumDTO = new AlbumDTO();
            albumDTO.setId(album.getId());
            albumDTO.setName(album.getName());
            albumDTO.setPrimaryArtistId(album.getPrimaryArtistId());
            albumDTO.setPrimaryArtistName(album.getPrimaryArtistName());
            albumDTO.setAlbumArtUrl(album.getAlbumArtPath());
            albumDTO.setReleaseYear(album.getReleaseYear());
            return albumDTO;
        }).toList();
        return albums;
    }

    @Override
    public Set<AlbumDTO> getAlbumsByArtist(Long artistId) {
        Artist artist =  artistRepository.findById(artistId).orElse(null);
        if(artist == null) throw new ResourceNotFound("Artist with artistId: " + artistId + " not found");
        List<AlbumSummary> albumSummaries = albumRepository.findAllByArtistId(artistId);

        return albumSummaries.stream().map(album -> {
            AlbumDTO albumDTO = new AlbumDTO();
            albumDTO.setId(album.getId());
            albumDTO.setName(album.getName());
            albumDTO.setPrimaryArtistId(album.getPrimaryArtistId());
            albumDTO.setPrimaryArtistName(album.getPrimaryArtistName());
            albumDTO.setAlbumArtUrl(album.getAlbumArtPath());
            albumDTO.setReleaseYear(album.getReleaseYear());
            return albumDTO;
        }).collect(Collectors.toSet());

    }

    @Override
    public AlbumDTO getAlbumById(Long albumId) {
        Optional<AlbumSummary> albumSummary = albumRepository.findAlbumProjectedBy(albumId);
        if (albumSummary.isEmpty()) throw new ResourceNotFound("Album with albumId: " + albumId + " not found");
        AlbumSummary summary = albumSummary.get();
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setId(summary.getId());
        albumDTO.setName(summary.getName());
        albumDTO.setPrimaryArtistId(summary.getPrimaryArtistId());
        albumDTO.setPrimaryArtistName(summary.getPrimaryArtistName());
        albumDTO.setAlbumArtUrl(summary.getAlbumArtPath());
        albumDTO.setReleaseYear(summary.getReleaseYear());
        return albumDTO;
    }
}
