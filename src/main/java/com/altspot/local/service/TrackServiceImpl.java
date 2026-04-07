package com.altspot.local.service;


import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.NullParameterException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import com.altspot.local.model.Track;
import com.altspot.local.payload.*;
import com.altspot.local.repository.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.PathVariable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements TrackService {

    private static final Logger logger = LoggerFactory.getLogger(TrackServiceImpl.class);

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    public TrackServiceImpl(TrackRepository trackRepository, AlbumRepository albumRepository, ArtistRepository artistRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;

    }

    @Override
    public TrackDTO getTrackById(Long id){
        TrackSummary trackSummary = trackRepository.getTrackSummaryById(id);
        if(trackSummary == null){ throw new ResourceNotFound("Track not found"); }

        List<ArtistSummary> artistSummaries = trackRepository.findArtistsByTrackId(id);

        Set<ArtistDTO> artistDTOs;

        if(artistSummaries.isEmpty()) artistDTOs = Collections.emptySet();

        else artistDTOs = artistSummaries.stream()
                .map(artistSummary -> {
                    ArtistDTO artistDTO = new ArtistDTO();
                    artistDTO.setId(artistSummary.getArtistId());
                    artistDTO.setName(artistSummary.getArtistName());
                    return artistDTO;
                }).collect(Collectors.toSet());

        return trackSummaryToDTO(trackSummary , artistDTOs);

    }

    @Override
    public List<TrackDTO> getSinglesByArtist(Long artistId) {
        if(artistId == null) throw new NullParameterException("Artist id is null");

        List<TrackSummary> trackSummaries = trackRepository.findAllSinglesByArtistId(artistId);

        return buildTrackDTOsFromTrackSummaries(trackSummaries);
    }

    @Override
    public ResponseEntity<Resource> stream(Long trackId, String range) throws IOException {
        if(trackId == null) throw new NullParameterException("Track id is null");
        if(range == null) throw new NullParameterException("Range is null");

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

        Page<TrackSummary> trackPage = trackRepository.findAllProjected(pageDetails);

        List<TrackSummary> tracks = trackPage.getContent();

        if (tracks.isEmpty()) throw new GeneralException("No tracks available");

        List<TrackDTO> content = buildTrackDTOsFromTrackSummaries(tracks);

        PageResult<TrackDTO> trackResponse = new PageResult<TrackDTO>();
        trackResponse.setContent(content);
        trackResponse.setPageNumber(trackPage.getNumber());
        trackResponse.setTotalPages(trackPage.getTotalPages());
        trackResponse.setTotalElements(trackPage.getTotalElements());
        trackResponse.setLastPage(trackPage.isLast());
        trackResponse.setPageSize(trackPage.getSize());

        return trackResponse;
    }

    @Override
    public List<AlbumTrackDTO> getTracksByAlbum(Long albumId) {
        Album album =  albumRepository.findById(albumId).orElse(null);
        if(album == null) throw new ResourceNotFound("Album with albumId: " + albumId + " not found");
        List<AlbumTrackSummary> albumTrackSummaries = trackRepository.findAllByAlbumId(albumId);

        List<Long> trackIds = albumTrackSummaries.stream().map(AlbumTrackSummary::getId).toList();
        List<TrackArtistFlatRow> trackArtistFlatRows = trackRepository.findArtistsByTrackIds(trackIds);

        Map<Long, Set<ArtistDTO>> artistMap = new HashMap<>();

        for (var row : trackArtistFlatRows) {
            artistMap
                    .computeIfAbsent(row.getTrackId(), k -> new HashSet<>())
                    .add(new ArtistDTO(row.getArtistId(), row.getArtistName()));
        }

        List<AlbumTrackDTO> dtos = new ArrayList<>(albumTrackSummaries.stream().map(albumTrackSummary -> {
            AlbumTrackDTO trackDTO = new AlbumTrackDTO();
            trackDTO.setId(albumTrackSummary.getId());
            trackDTO.setName(getTrackName(albumTrackSummary.getName()));
            trackDTO.setAlbumPosition(albumTrackSummary.getAlbumPosition());
            trackDTO.setDurationSeconds(albumTrackSummary.getDurationSeconds());
            trackDTO.setAlbumName(albumTrackSummary.getAlbumName());
            trackDTO.setAlbumId(albumTrackSummary.getAlbumId());
            trackDTO.setArtists(artistMap.getOrDefault(albumTrackSummary.getId(), Set.of()));

            return trackDTO;
        }).toList());

        dtos.sort(Comparator.comparing(AlbumTrackDTO::getAlbumPosition));
        return dtos;

    }

    @Override
    public List<TrackDTO> getTracksByKeyword(String keyword){
        String normalizedKeyword = keyword == null
                ? ""
                : keyword.trim().toLowerCase();

        if(normalizedKeyword.isEmpty()) throw new NullParameterException("Keyword is empty");

        List<TrackSummary> trackSummaries = trackRepository.searchByPrefix(normalizedKeyword);

        return buildTrackDTOsFromTrackSummaries(trackSummaries);
    }

    @Override
    public List<TrackDTO> getTracksByArtist(Long artistId) {
        if(artistId == null) throw new NullParameterException("Artist id is null");

        Artist artist = artistRepository.findById(artistId).orElse(null);

        if(artist == null) throw new ResourceNotFound("Artist with id: " + artistId + " not found");

        List<TrackSummary> trackSummaries = trackRepository.findAllByArtistId(artistId);

        return buildTrackDTOsFromTrackSummaries(trackSummaries);
    }

    private String getFilePathFromDB(Long trackId) {
        if (trackId == null) throw new NullParameterException("Track id is null");

        Optional<Track> opt = trackRepository.findById(trackId);

        if (opt.isEmpty() || emptyToNull(opt.get().getFilePath()) == null) {
            throw new ResourceNotFound("Track with trackId" + trackId + " not found");
        }
        return opt.get().getFilePath();
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private String getTrackName(String name){
        int index = name.indexOf("-");
        if(index == -1) return name;
        return name.substring(0, index);
    }

    private TrackDTO trackSummaryToDTO(TrackSummary trackSummary , Set<ArtistDTO> artistDTOS) {
        TrackDTO trackDTO = new TrackDTO();
        trackDTO.setId(trackSummary.getId());
        trackDTO.setName(getTrackName(trackSummary.getName()));
        trackDTO.setDurationSeconds(trackSummary.getDurationSeconds());
        trackDTO.setAlbumName(trackSummary.getAlbumName());
        trackDTO.setAlbumId(trackSummary.getAlbumId());
        trackDTO.setArtists(artistDTOS);

        return trackDTO;
    }

    private Map<Long , Set<ArtistDTO>> createArtistMapFromTrackIds(List<TrackSummary> trackSummaries){
        List<Long> trackIds = trackSummaries.stream().map(TrackSummary::getId).toList();
        List<TrackArtistFlatRow> trackArtistFlatRows = trackRepository.findArtistsByTrackIds(trackIds);

        Map<Long, Set<ArtistDTO>> artistMap = new HashMap<>();

        for (var row : trackArtistFlatRows) {
            artistMap
                    .computeIfAbsent(row.getTrackId(), k -> new HashSet<>())
                    .add(new ArtistDTO(row.getArtistId(), row.getArtistName()));
        }
        return  artistMap;
    }

    private List<TrackDTO> buildTrackDTOsFromTrackSummaries(List<TrackSummary> trackSummaries){
        if(trackSummaries.isEmpty()) return new ArrayList<>();

        Map<Long, Set<ArtistDTO>> artistMap = createArtistMapFromTrackIds(trackSummaries);

        return trackSummaries.stream()
                .map(track ->
                        trackSummaryToDTO(track , artistMap.getOrDefault(track.getId() , Set.of()))
                )
                .toList();
    }


}