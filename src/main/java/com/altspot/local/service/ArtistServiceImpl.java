package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.payload.*;
import com.altspot.local.repository.ArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistServiceImpl implements  ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistServiceImpl(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public PageResult<ArtistDTO> getArtists(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){

        Sort sortByAndOrder = sortDirection.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<ArtistSummary> artistPage = artistRepository.findAllProjectedBy(pageDetails);

        List<ArtistSummary> artists = artistPage.getContent();

        if (artists.isEmpty()) throw new GeneralException("No artists available");

        List<ArtistDTO> content = artists.stream()
                .map(artist -> {


                    ArtistDTO artistDTO = new ArtistDTO();
                    artistDTO.setId(artist.getArtistId());
                    artistDTO.setName(artist.getArtistName());
                    return artistDTO;
                })
                .toList();

        PageResult<ArtistDTO> artistResponse = new PageResult<>();
        artistResponse.setContent(content);
        artistResponse.setPageNumber(artistPage.getNumber());
        artistResponse.setTotalPages(artistPage.getTotalPages());
        artistResponse.setTotalElements(artistPage.getTotalElements());
        artistResponse.setLastPage(artistPage.isLast());
        artistResponse.setPageSize(artistPage.getSize());

        return artistResponse;
    }

    @Override
    public List<ArtistDTO> getArtistsByKeyword(String keyword) {
        String normalizedKeyword = keyword == null
                ? ""
                : keyword.trim().toLowerCase();

        if(normalizedKeyword.isEmpty()) throw new GeneralException("Keyword is empty");

        List<ArtistSummary> albumSummaries = artistRepository.searchByPrefix(normalizedKeyword);
        List<ArtistDTO> artists =  albumSummaries.stream().map(artist -> {
            ArtistDTO artistDTO = new ArtistDTO();
            artistDTO.setId(artist.getArtistId());
            artistDTO.setName(artist.getArtistName());

            return artistDTO;
        }).toList();
        return artists;
    }
}
