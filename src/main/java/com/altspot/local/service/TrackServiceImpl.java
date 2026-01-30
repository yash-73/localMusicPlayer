package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Track;
import com.altspot.local.payload.*;
import com.altspot.local.repository.TrackRepository;
import jakarta.transaction.Transactional;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class TrackServiceImpl implements TrackService {

    @Value("${music.directory.path}")
    private String musicDirectoryPath;

    private final TrackRepository trackRepository;

    private final ModelMapper modelMapper;

    public TrackServiceImpl(TrackRepository trackRepository, ModelMapper modelMapper) {
        this.trackRepository = trackRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public RescanResult rescan() throws IOException {

        Set<String> dbPaths = trackRepository.findAllFilePaths();
        Set<String> fsPaths = new HashSet<>();

        AtomicInteger inserted = new AtomicInteger();
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger deleted = new AtomicInteger();

        try (Stream<Path> stream = Files.walk(Paths.get(musicDirectoryPath))) {

            stream.filter(Files::isRegularFile)
                    .filter(this::isMusicFile)
                    .forEach(path -> {

                        String absPath = path.toAbsolutePath().toString();
                        fsPaths.add(absPath);

                        Optional<Track> opt = trackRepository.findByFilePath(absPath);

                        try {
                            if (opt.isEmpty()) {
                                Track track = buildTrack(path.toFile());
                                trackRepository.save(track);
                                inserted.incrementAndGet();
                            } else {
                                Track track = opt.get();
                                track.setLastScannedAt(Instant.now());
                                updated.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // bad file, log & skip
                            System.out.println("Failed to read: " + absPath);
                        }
                    });

        }

        // delete DB entries missing from filesystem
        for (String dbPath : dbPaths) {
            if (!fsPaths.contains(dbPath)) {
                trackRepository.deleteByFilePath(dbPath);
                deleted.incrementAndGet();
            }
        }

        return new RescanResult(
                inserted.get(),
                deleted.get(),
                updated.get()
        );
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
                .map(track -> modelMapper.map(track, TrackDTO.class))
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

    @Override
    public PageResult<AlbumDTO> getAlbums(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) throws IOException {
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<AlbumSummary> albumPage =
                trackRepository.findAlbumSummaries(pageable);

        List<AlbumSummary> albums = albumPage.getContent();

        if (albums.isEmpty()) {
            throw new GeneralException("No albums available");
        }

        List<AlbumDTO> content = albums.stream()
                .map(album -> modelMapper.map(album , AlbumDTO.class))
                .toList();

        PageResult<AlbumDTO> response = new PageResult<>();
        response.setContent(content); // projections are already DTO-shaped
        response.setPageNumber(albumPage.getNumber());
        response.setTotalPages(albumPage.getTotalPages());
        response.setTotalElements(albumPage.getTotalElements());
        response.setLastPage(albumPage.isLast());
        response.setPageSize(albumPage.getSize());

        return response;
    }


    private String getFilePathFromDB(Long trackId) {
        Optional<Track> opt = trackRepository.findById(trackId);
        if (opt.isEmpty() || emptyToNull(opt.get().getFilePath()) == null) {
            throw new ResourceNotFound("Track with trackId" + trackId + " not found");
        }
        return opt.get().getFilePath();
}


    private boolean isMusicFile(Path path) {
        String fileName =  path.getFileName()
                .toString()
                .toLowerCase();
        return fileName.endsWith(".mp3") || fileName.endsWith(".wav");
    }

    private Track buildTrack(File file) throws Exception {

        AudioFile audio = AudioFileIO.read(file);
        Tag tag = audio.getTag();
        AudioHeader header = audio.getAudioHeader();

        Track track = new Track();

        if (tag != null) {
            track.setTitle(emptyToNull(tag.getFirst(FieldKey.TITLE)));
            track.setArtist(emptyToNull(tag.getFirst(FieldKey.ARTIST)));
            track.setAlbum(emptyToNull(tag.getFirst(FieldKey.ALBUM)));
            track.setGenre(emptyToNull(tag.getFirst(FieldKey.GENRE)));
        }

        track.setDurationSeconds(header.getTrackLength());

        try {
            track.setSampleRate(Integer.parseInt(header.getSampleRate()));
        } catch (NumberFormatException e) {
            track.setSampleRate(null);
        }

        track.setFilePath(file.getAbsolutePath());
        track.setFileSize(file.length());
        track.setLastScannedAt(Instant.now());

        return track;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }


}
