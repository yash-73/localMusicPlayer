package com.altspot.local.service;

import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Track;
import com.altspot.local.payload.RescanResult;
import com.altspot.local.repository.TrackRepository;
import jakarta.transaction.Transactional;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Transactional
public class TrackServiceImpl implements TrackService {

    @Value("${music.directory.path}")
    private String musicDirectoryPath;

    private final TrackRepository trackRepository;

    public TrackServiceImpl(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
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
                    .filter(this::isMp3)
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



    private String getFilePathFromDB(Long trackId) {
    // lookup from MySQL
        Optional<Track> opt = trackRepository.findById(trackId);
        if (opt.isEmpty() || emptyToNull(opt.get().getFilePath()) == null) {
            throw new ResourceNotFound("Track with trackId" + trackId + " not found");
        }
        return opt.get().getFilePath();
}


    private boolean isMp3(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(".mp3");
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
