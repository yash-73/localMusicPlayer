package com.altspot.local.service;


import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Album;
import com.altspot.local.model.Track;
import com.altspot.local.payload.*;
import com.altspot.local.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements TrackService {

    private final ModelMapper modelMapper;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final TrackArtistMaintenanceRepository trackArtistMaintenanceRepository;
    private final AlbumArtistMaintenanceRepository albumArtistMaintenanceRepository;

    public TrackServiceImpl(ModelMapper modelMapper, TrackRepository trackRepository, AlbumRepository albumRepository, ArtistRepository artistRepository,
                            TrackArtistMaintenanceRepository trackArtistMaintenanceRepository, AlbumArtistMaintenanceRepository albumArtistMaintenanceRepository) {
        this.modelMapper = modelMapper;
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.trackArtistMaintenanceRepository = trackArtistMaintenanceRepository;
        this.albumArtistMaintenanceRepository = albumArtistMaintenanceRepository;

    }

    @Override
    public ResponseEntity<Resource> stream(Long trackId, String range) throws IOException {

        Path path = Path.of(getFilePathFromDB(trackId));
        long fileSize = Files.size(path);

        long start = 0;
        long end = fileSize - 1;

        if (range != null && range.startsWith("bytes=")) {
            String[] parts = range.substring(6).split("-");
            start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isEmpty()) {
                end = Long.parseLong(parts[1]);
            }
        }

        long contentLength = end - start + 1;

        RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
        raf.seek(start);

        InputStream inputStream = new FileInputStream(raf.getFD());
        Resource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path));

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(resource);
    }

    @Override
    public PageResult<TrackDTO> getTracks(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException {

        Sort sortByAndOrder = sortDirection.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<TrackSummary> trackPage = trackRepository.findAllProjectedBy(pageDetails);

        List<TrackSummary> tracks = trackPage.getContent();

        if (tracks.isEmpty()) throw new GeneralException("No tracks available");

        List<TrackDTO> content = tracks.stream()
                .map(track -> {
                    TrackDTO trackDTO = new TrackDTO();
                    trackDTO.setId(track.getId());
                    trackDTO.setName(track.getName());
                    trackDTO.setDurationSeconds(track.getDurationSeconds());
                    trackDTO.setArtists(
                            track.getArtists().stream().map(artist -> new ArtistDTO(artist.getId(), artist.getName())).collect(Collectors.toSet())
                    );

                    ArtistDTO primaryArtist = new  ArtistDTO();
                    primaryArtist.setId(track.getAlbum().getPrimaryArtist().getId());
                    primaryArtist.setName(track.getAlbum().getPrimaryArtist().getName());

                    trackDTO.setAlbum(new AlbumDTO(track.getAlbum().getId() , track.getAlbum().getName() , primaryArtist));

                    return trackDTO;


                })
                .toList();

        PageResult<TrackDTO> trackResponse = new PageResult<TrackDTO>();
        trackResponse.setContent(content);
        trackResponse.setPageNumber(trackPage.getNumber());
        trackResponse.setTotalPages(trackPage.getTotalPages());
        trackResponse.setTotalElements(trackPage.getTotalElements());
        trackResponse.setLastPage(trackPage.isLast());
        trackResponse.setPageSize(trackPage.getSize());

        return trackResponse;
    }

    private String getFilePathFromDB(Long trackId) {
        Optional<Track> opt = trackRepository.findById(trackId);
        if (opt.isEmpty() || emptyToNull(opt.get().getFilePath()) == null) {
            throw new ResourceNotFound("Track with trackId" + trackId + " not found");
        }
        return opt.get().getFilePath();
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }



}