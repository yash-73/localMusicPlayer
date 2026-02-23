package com.altspot.local.service;

import com.altspot.local.model.*;
import com.altspot.local.payload.RescanResult;
import com.altspot.local.repository.*;
import jakarta.transaction.Transactional;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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

    public RescanServiceImpl(
            TrackRepository trackRepository,
            AlbumRepository albumRepository,
            ArtistRepository artistRepository,
            TrackArtistMaintenanceRepository trackArtistMaintenanceRepository,
            AlbumArtistMaintenanceRepository albumArtistMaintenanceRepository
    ) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.trackArtistMaintenanceRepository = trackArtistMaintenanceRepository;
        this.albumArtistMaintenanceRepository = albumArtistMaintenanceRepository;
    }

    @Value("${music.directory.path}")
    private String musicDirectoryPath;

    private static final String ALBUM_ART_DIR =
            "/home/yash/projects/local/src/main/resources/static/album_art/";

    public RescanResult rescan() throws Exception {

        Set<String> dbPaths = trackRepository.findAllFilePaths();
        Set<String> fsPaths = new HashSet<>();

        int inserted = 0, updated = 0, deleted = 0;

        try (Stream<Path> stream = Files.walk(Paths.get(musicDirectoryPath))) {

            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(this::isMusicFile)
                    .toList()) {

                File file = path.toFile();
                String absPath = file.getAbsolutePath();
                fsPaths.add(absPath);

                Optional<Track> existingOpt =
                        trackRepository.findByFilePath(absPath);

                if (existingOpt.isPresent() &&
                        Objects.equals(existingOpt.get().getFileSize(), file.length())) {

                    existingOpt.get().setLastScannedAt(Instant.now());
                    updated++;
                    continue;
                }

                TrackMeta meta;
                try {
                    meta = extractMetadata(file);
                } catch (Exception e) {
                    continue;
                }

                if (existingOpt.isEmpty()) {
                    insertTrack(meta);
                    inserted++;
                } else {
                    updateTrack(existingOpt.get(), meta);
                    updated++;
                }
            }
        }

        for (String dbPath : dbPaths) {
            if (!fsPaths.contains(dbPath)) {
                deleteTrackAndCleanup(dbPath);
                deleted++;
            }
        }

        albumArtistMaintenanceRepository.rebuildAlbumArtist();

        return new RescanResult(inserted, deleted, updated);
    }

    @Transactional
    protected void deleteTrackAndCleanup(String filePath) {

        Track track = trackRepository.findByFilePath(filePath).orElseThrow();

        Long albumId = track.getAlbum().getId();
        Set<Long> artistIds = track.getArtists()
                .stream().map(Artist::getId).collect(Collectors.toSet());

        trackRepository.delete(track);

        if (trackRepository.countByAlbum_Id(albumId) == 0) {

            Album album = albumRepository.findById(albumId).orElse(null);

            if (album != null && album.getAlbumArtPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(album.getAlbumArtPath()));
                } catch (Exception ignored) {}
            }

            albumRepository.deleteById(albumId);
        }

        for (Long artistId : artistIds) {
            if (trackArtistMaintenanceRepository.countByArtistId(artistId) == 0)
                artistRepository.deleteById(artistId);
        }
    }

    @Transactional
    protected void insertTrack(TrackMeta meta) {

        Set<Artist> trackArtists = resolveArtists(meta.artistNames());

        Artist primaryArtist = (meta.albumArtistName() != null)
                ? resolveArtists(List.of(meta.albumArtistName())).iterator().next()
                : trackArtists.iterator().next();

        Album album = resolveAlbum(
                meta.albumName(),
                meta.releaseYear(),
                primaryArtist,
                meta.tag()
        );

        Track track = new Track();
        track.setFilePath(meta.filePath());
        track.setName(meta.title());
        track.setGenre(meta.genre());
        track.setDurationSeconds(meta.durationSeconds());
        track.setSampleRate(meta.sampleRate());
        track.setFileSize(meta.fileSize());
        track.setLastScannedAt(Instant.now());
        track.setAlbum(album);
        track.setArtists(trackArtists);
        track.setAlbumPosition(meta.trackNumber());

        trackRepository.save(track);
    }

    @Transactional
    protected void updateTrack(Track track, TrackMeta meta) {

        Set<Artist> trackArtists = resolveArtists(meta.artistNames());

        Artist primaryArtist = (meta.albumArtistName() != null)
                ? resolveArtists(List.of(meta.albumArtistName())).iterator().next()
                : trackArtists.iterator().next();

        Album album = resolveAlbum(
                meta.albumName(),
                meta.releaseYear(),
                primaryArtist,
                meta.tag()
        );

        track.setName(meta.title());
        track.setGenre(meta.genre());
        track.setDurationSeconds(meta.durationSeconds());
        track.setSampleRate(meta.sampleRate());
        track.setFileSize(meta.fileSize());
        track.setLastScannedAt(Instant.now());
        track.setAlbum(album);
        track.setAlbumPosition(meta.trackNumber());

        track.getArtists().clear();
        track.getArtists().addAll(trackArtists);

        trackRepository.save(track);
    }

    protected Set<Artist> resolveArtists(List<String> names) {
        Set<Artist> result = new HashSet<>();
        for (String name : names) {
            Artist artist = artistRepository.findByName(name)
                    .orElseGet(() -> artistRepository.save(new Artist(name)));
            result.add(artist);
        }
        return result;
    }

    @Transactional
    protected Album resolveAlbum(
            String albumName,
            Integer releaseYear,
            Artist primaryArtist,
            Tag tag
    ) {

        // 1. Try exact match (name + artist + year if present)
        Optional<Album> existing;

        if (releaseYear != null) {
            existing = albumRepository
                    .findByNameAndPrimaryArtist_IdAndReleaseYear(
                            albumName,
                            primaryArtist.getId(),
                            releaseYear
                    );
        } else {
            existing = albumRepository
                    .findByNameAndPrimaryArtist_Id(
                            albumName,
                            primaryArtist.getId()
                    );
        }

        if (existing.isPresent()) {
            Album album = existing.get();
            attachAlbumArtIfMissing(album, tag);
            return album;
        }

        // 2. If year is present but exact match not found,
        //    check if an album exists with NULL year.
        if (releaseYear != null) {

            Optional<Album> nullYearMatch =
                    albumRepository.findByNameAndPrimaryArtist_IdAndReleaseYear(
                            albumName,
                            primaryArtist.getId(),
                            null
                    );

            if (nullYearMatch.isPresent()) {
                Album album = nullYearMatch.get();
                album.setReleaseYear(releaseYear);
                albumRepository.save(album);

                attachAlbumArtIfMissing(album, tag);
                return album;
            }
        }

        // 3. Otherwise create new album
        return createAlbum(albumName, primaryArtist, releaseYear, tag);
    }

    private void attachAlbumArtIfMissing(Album album, Tag tag) {
        if (album.getAlbumArtPath() == null &&
                tag != null &&
                tag.getFirstArtwork() != null) {

            String artPath = extractAndStoreAlbumArt(tag, album.getId());
            album.setAlbumArtPath(artPath);
            albumRepository.save(album);
        }
    }

    private Album createAlbum(
            String albumName,
            Artist primaryArtist,
            Integer releaseYear,
            Tag tag
    ) {

        Album album = new Album();
        album.setName(albumName);
        album.setPrimaryArtist(primaryArtist);
        album.setReleaseYear(releaseYear);

        album = albumRepository.save(album);

        if (tag != null && tag.getFirstArtwork() != null) {
            String artPath = extractAndStoreAlbumArt(tag, album.getId());
            album.setAlbumArtPath(artPath);
            albumRepository.save(album);
        }

        return album;
    }

    private String extractAndStoreAlbumArt(Tag tag, Long albumId) {

        if (tag == null || tag.getFirstArtwork() == null) return null;

        try {
            byte[] data = tag.getFirstArtwork().getBinaryData();
            String mime = tag.getFirstArtwork().getMimeType();

            String extension = (mime != null && mime.toLowerCase().contains("png"))
                    ? ".png" : ".jpg";

            Path dir = Paths.get(ALBUM_ART_DIR);
            Files.createDirectories(dir);

            Path filePath = dir.resolve(albumId + extension);
            Files.write(filePath, data,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            return filePath.toString();

        } catch (Exception e) {
            return null;
        }
    }

    protected TrackMeta extractMetadata(File file) throws Exception {

        AudioFile audio = AudioFileIO.read(file);
        Tag tag = audio.getTag();
        AudioHeader header = audio.getAudioHeader();

        String album = tag != null ? tag.getFirst(FieldKey.ALBUM) : null;
        String artistsRaw = tag != null ? tag.getFirst(FieldKey.ARTIST) : null;
        String albumArtistRaw = tag != null ? tag.getFirst(FieldKey.ALBUM_ARTIST) : null;
        String trackRaw = tag != null ? tag.getFirst(FieldKey.TRACK) : null;

        Integer trackNumber = parseTrackNumber(trackRaw);

        List<String> artists = (artistsRaw == null)
                ? List.of("Unknown")
                : Arrays.stream(artistsRaw.split("/"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        Integer year = tag != null ? extractAlbumYear(tag) : null;

        return new TrackMeta(
                file.getAbsolutePath(),
                tag != null ? tag.getFirst(FieldKey.TITLE) : null,
                album != null ? album : "Unknown Album",
                albumArtistRaw,
                artists,
                year,
                tag != null ? tag.getFirst(FieldKey.GENRE) : null,
                header.getTrackLength(),
                Integer.parseInt(header.getSampleRate()),
                file.length(),
                trackNumber,
                tag
        );
    }

    private Integer parseTrackNumber(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String first = raw.split("/")[0].trim();
        if (first.matches("\\d+")) return Integer.parseInt(first);
        return null;
    }

    private Integer extractAlbumYear(Tag tag) {
        if (tag == null) return null;
        String raw = tag.getFirst(FieldKey.YEAR);
        if (raw == null) return null;
        if (raw.length() >= 4)
            return Integer.parseInt(raw.substring(0, 4));
        return null;
    }

    private boolean isMusicFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".mp3")
                || name.endsWith(".flac")
                || name.endsWith(".wav");
    }

    public record TrackMeta(
            String filePath,
            String title,
            String albumName,
            String albumArtistName,
            List<String> artistNames,
            Integer releaseYear,
            String genre,
            Integer durationSeconds,
            Integer sampleRate,
            long fileSize,
            Integer trackNumber,
            Tag tag
    ) {}
}