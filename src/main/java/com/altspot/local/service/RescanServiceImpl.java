package com.altspot.local.service;


import com.altspot.local.model.*;
import com.altspot.local.payload.RescanResult;
import com.altspot.local.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RescanServiceImpl {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final TrackArtistMaintenanceRepository trackArtistMaintenanceRepository;
    private final AlbumArtistMaintenanceRepository albumArtistMaintenanceRepository;

    public RescanServiceImpl (TrackRepository trackRepository ,AlbumRepository albumRepository,
                              ArtistRepository artistRepository, TrackArtistMaintenanceRepository trackArtistMaintenanceRepository, AlbumArtistMaintenanceRepository albumArtistMaintenanceRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.trackArtistMaintenanceRepository = trackArtistMaintenanceRepository;
        this.albumArtistMaintenanceRepository = albumArtistMaintenanceRepository;
    }

    @Value("${music.directory.path}")
    private String musicDirectoryPath;

    /* ======================= PUBLIC API ======================= */

    public RescanResult rescan() throws IOException {

        Set<String> dbPaths = trackRepository.findAllFilePaths();
        Set<String> fsPaths = new HashSet<>();

        int inserted = 0, updated = 0, deleted = 0;

        try (Stream<Path> stream = Files.walk(Paths.get(musicDirectoryPath))) {

            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(this::isMusicFile)
                    .toList()) {

                String absPath = path.toAbsolutePath().toString();
                fsPaths.add(absPath);

                Optional<Track> existing = trackRepository.findByFilePath(absPath);

                TrackMeta meta;
                try {
                    meta = extractMetadata(path.toFile());
                } catch (Exception e) {
                    System.out.println("Failed to read metadata: " + absPath);
                    continue;
                }

                if (existing.isEmpty()) {
                    insertTrack(meta);
                    inserted++;
                } else {
                    updateTrack(existing.get(), meta);
                    updated++;
                }
            }
        }

        /* ---------- deletions ---------- */
        for (String dbPath : dbPaths) {
            if (!fsPaths.contains(dbPath)) {
                deleteTrackAndCleanup(dbPath);
                deleted++;
            }
        }

        /* ---------- rebuild derived table ---------- */
        albumArtistMaintenanceRepository.rebuildAlbumArtist();

        return new RescanResult(inserted, deleted, updated);
    }

    /* ======================= INSERT / UPDATE ======================= */


    @Transactional
    protected void insertTrack(TrackMeta meta) {

        Set<Artist> artists = resolveArtists(meta.artistNames());
        Artist primaryArtist = artists.iterator().next();

        Album album = resolveAlbum(meta.albumName(), primaryArtist);

        Track track = new Track();
        track.setFilePath(meta.filePath());
        track.setName(meta.title());
        track.setGenre(meta.genre());
        track.setDurationSeconds(meta.durationSeconds());
        track.setSampleRate(meta.sampleRate());
        track.setFileSize(meta.fileSize());
        track.setLastScannedAt(Instant.now());
        track.setAlbum(album);
        track.setArtists(artists);

        trackRepository.save(track);
    }

    @Transactional
    protected void updateTrack(Track track, TrackMeta meta) {

        Set<Artist> artists = resolveArtists(meta.artistNames());
        Artist primaryArtist = artists.iterator().next();
        Album album = resolveAlbum(meta.albumName(), primaryArtist);

        track.setName(meta.title());
        track.setGenre(meta.genre());
        track.setDurationSeconds(meta.durationSeconds());
        track.setSampleRate(meta.sampleRate());
        track.setFileSize(meta.fileSize());
        track.setLastScannedAt(Instant.now());
        track.setAlbum(album);

        track.getArtists().clear();
        track.getArtists().addAll(artists);

        trackRepository.save(track);
    }

    /* ======================= DELETE + CLEANUP ======================= */

    @Transactional
    protected void deleteTrackAndCleanup(String filePath) {

        Track track = trackRepository.findByFilePath(filePath)
                .orElseThrow();

        Long albumId = track.getAlbum().getId();
        Set<Long> artistIds = track.getArtists()
                .stream()
                .map(Artist::getId)
                .collect(Collectors.toSet());

        trackRepository.delete(track);

        if (trackRepository.countByAlbum_Id(albumId) == 0) {
            albumRepository.deleteById(albumId);
        }

        for (Long artistId : artistIds) {
            if (trackArtistMaintenanceRepository.countByArtistId(artistId) == 0) {
                artistRepository.deleteById(artistId);
            }
        }
    }

    /* ======================= RESOLUTION HELPERS ======================= */

    protected Set<Artist> resolveArtists(List<String> names) {
        Set<Artist> result = new HashSet<>();
        for (String name : names) {
            Artist artist = artistRepository.findByName(name)
                    .orElseGet(() -> artistRepository.save(new Artist(name)));
            result.add(artist);
        }
        return result;
    }

    protected Album resolveAlbum(String albumName, Artist primaryArtist) {
        return albumRepository
                .findByNameAndPrimaryArtist(albumName, primaryArtist)
                .orElseGet(() -> {
                    Album album = new Album();
                    album.setName(albumName);
                    album.setPrimaryArtist(primaryArtist);
                    return albumRepository.save(album);
                });
    }

    /* ======================= METADATA ======================= */

    protected TrackMeta extractMetadata(File file) throws Exception {

        AudioFile audio = AudioFileIO.read(file);
        Tag tag = audio.getTag();
        AudioHeader header = audio.getAudioHeader();

        String title = tag != null ? emptyToNull(tag.getFirst(FieldKey.TITLE)) : null;
        String album = tag != null ? emptyToNull(tag.getFirst(FieldKey.ALBUM)) : null;
        String artistsRaw = tag != null ? emptyToNull(tag.getFirst(FieldKey.ARTIST)) : null;
        String genre = tag != null ? emptyToNull(tag.getFirst(FieldKey.GENRE)) : null;

        List<String> artists = artistsRaw == null
                ? List.of("Unknown")
                : Arrays.stream(artistsRaw.split("/"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return new TrackMeta(
                file.getAbsolutePath(),
                title,
                album != null ? album : "Unknown Album",
                artists,
                genre,
                header.getTrackLength(),
                parseIntOrNull(header.getSampleRate()),
                file.length()
        );
    }

    /* ======================= UTIL ======================= */

    private boolean isMusicFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac");
    }

    private Integer parseIntOrNull(String v) {
        try { return Integer.parseInt(v); }
        catch (Exception e) { return null; }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /* ======================= INTERNAL DTO ======================= */

    public record TrackMeta(
            String filePath,
            String title,
            String albumName,
            List<String> artistNames,
            String genre,
            Integer durationSeconds,
            Integer sampleRate,
            long fileSize
    ) {}
}
