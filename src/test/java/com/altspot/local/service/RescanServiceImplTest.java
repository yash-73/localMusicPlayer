package com.altspot.local.service;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import com.altspot.local.model.Track;
import com.altspot.local.payload.RescanResult;
import com.altspot.local.repository.AlbumRepository;
import com.altspot.local.repository.ArtistRepository;
import com.altspot.local.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
class RescanServiceImplTest {

    @Autowired
    private RescanServiceImpl rescanService;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Spy
    private RescanServiceImpl spyRescanService;

    @TempDir
    Path musicDir;

    @BeforeEach
    void setup() throws Exception {
        // create fake music files
        Files.createFile(musicDir.resolve("song1.mp3"));
        Files.createFile(musicDir.resolve("song2.mp3"));

        // override music directory path
        var field = RescanServiceImpl.class
                .getDeclaredField("musicDirectoryPath");
        field.setAccessible(true);
        field.set(spyRescanService, musicDir.toString());

        // mock metadata extraction
        doReturn(fakeMeta("song1.mp3", "Track 1", "Album A", "Artist A/Artist B"))
                .when(spyRescanService)
                .extractMetadata(any(File.class));

        doReturn(fakeMeta("song2.mp3", "Track 2", "Album A", "Artist A"))
                .when(spyRescanService)
                .extractMetadata(any(File.class));
    }

    @Test
    void rescan_shouldInsertTracksAlbumsArtists() throws Exception {

        RescanResult result = rescanService.rescan();

        assertEquals(2, trackRepository.count(), "tracks");
        assertEquals(1, albumRepository.count(), "albums");
        assertEquals(2, artistRepository.count(), "artists");

        Album album = albumRepository.findAll().get(0);
        assertEquals("Album A", album.getName());

        List<Artist> artists = artistRepository.findAll();
        assertEquals(2, artists.size());

        assertEquals(2, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.deleted());
    }

    @Test
    void rescan_shouldDeleteMissingTracksAndCleanup() throws Exception {

        // initial scan
        rescanService.rescan();

        // delete one file from filesystem
        Files.delete(musicDir.resolve("song2.mp3"));

        // now only one metadata response matters
        doReturn(fakeMeta("song1.mp3", "Track 1", "Album A", "Artist A"))
                .when(spyRescanService)
                .extractMetadata(any(File.class));

        RescanResult result = rescanService.rescan();

        assertEquals(1, trackRepository.count());
        assertEquals(1, albumRepository.count());
        assertEquals(1, artistRepository.count());

        Track remaining = trackRepository.findAll().get(0);
        assertEquals("Track 1", remaining.getName());

        assertEquals(1, result.deleted());
    }

    /* ---------- helpers ---------- */

    private RescanServiceImpl.TrackMeta fakeMeta(
            String fileName,
            String title,
            String album,
            String artists
    ) {
        return new RescanServiceImpl.TrackMeta(
                musicDir.resolve(fileName).toString(),
                title,
                album,
                List.of(artists.split("/")),
                "Rock",
                180,
                44100,
                123456
        );
    }
}
