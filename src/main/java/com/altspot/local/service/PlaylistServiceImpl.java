package com.altspot.local.service;

import com.altspot.local.config.userdetails.UserDetailsImpl;
import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.Playlist;
import com.altspot.local.model.Track;
import com.altspot.local.model.User;
import com.altspot.local.payload.*;
import com.altspot.local.repository.PlaylistItemRepository;
import com.altspot.local.repository.PlaylistRepository;
import com.altspot.local.repository.TrackRepository;
import com.altspot.local.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {


    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final TrackRepository trackRepository;

    public PlaylistServiceImpl(UserRepository userRepository,  PlaylistRepository playlistRepository,
                               PlaylistItemRepository playlistItemRepository , TrackRepository trackRepository) {
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
        this.trackRepository = trackRepository;
    }



    //Get metadata of all playlists created by user
    @Override
    public List<PlaylistMeta> getAllPlaylistMetas(Authentication authentication) {
        if(authentication == null) {
            throw new GeneralException("Authentication is null");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if(userDetails == null) {
            throw new GeneralException("UserDetails is null");
        }

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if(user.isEmpty()) throw new ResourceNotFound("User not found");
        User currentUser = user.get();
        List<PlaylistMetaSummary> summaries = playlistRepository.findByUserId(currentUser.getId());

        return summaries.stream()
                .map(summary -> {
                    PlaylistMeta playlistMeta = new PlaylistMeta();
                    playlistMeta.setPlaylistId(summary.getPlaylistId());
                    playlistMeta.setPlaylistName(summary.getPlaylistName());

                    return playlistMeta;
                        }
                ).toList();

    }


    @Override
    public List<PlaylistItemDTO> getAllPlaylistItemsForPlaylist(Long playlistId) {
        if(!playlistRepository.existsById(playlistId)) throw new ResourceNotFound("Playlist not found");

        List<PlaylistItemSummary> summaries = playlistItemRepository.findAllByPlaylistId(playlistId);

        List<Long> trackIds = summaries.stream().map(PlaylistItemSummary::getTrackId).toList();

        List<TrackArtistFlatRow> trackArtistFlatRows = trackRepository.findArtistsByTrackIds(trackIds);

        Map<Long, Set<ArtistDTO>> artistMap = new HashMap<>();

        for (var row : trackArtistFlatRows) {
            artistMap
                    .computeIfAbsent(row.getTrackId(), k -> new HashSet<>())
                    .add(new ArtistDTO(row.getArtistId(), row.getArtistName()));
        }


        return summaries.stream().map(summary -> {
            PlaylistItemDTO playlistItemDTO = new PlaylistItemDTO();
            playlistItemDTO.setPlaylistItemId(summary.getPlaylistItemId());
            playlistItemDTO.setPosition(summary.getPosition());
            playlistItemDTO.setTrackId(summary.getTrackId());
            playlistItemDTO.setTrackName(summary.getTrackName());
            playlistItemDTO.setDurationSeconds(summary.getDurationSeconds());
            playlistItemDTO.setAlbumId(summary.getAlbumId());
            playlistItemDTO.setAlbumName(summary.getAlbumName());
            playlistItemDTO.setArtists(artistMap.getOrDefault(summary.getTrackId() , Set.of()));
            return playlistItemDTO;
        }).toList();

    }

    @Override
    public PlaylistDTO getPlaylistMetaById(Long playlistId) {
        PlaylistMetaSummary summary = playlistRepository.findByPlaylistId(playlistId);
        PlaylistDTO playlistDTO = new PlaylistDTO();
        playlistDTO.setPlaylistId(summary.getPlaylistId());
        playlistDTO.setPlaylistName(summary.getPlaylistName());
        playlistDTO.setPlaylistItemDTOList(getAllPlaylistItemsForPlaylist(summary.getPlaylistId()));
        return playlistDTO;
    }


    @Override
    @Transactional
    public PlaylistDTO createNewPlaylist(Authentication authentication, PlaylistMeta playlistMeta) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        assert userDetails != null;
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new GeneralException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(playlistMeta.getPlaylistName());
        playlist.setUser(currentUser);

        try {
            playlist = playlistRepository.save(playlist);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException("Playlist with this name already exists");
        }

        return new PlaylistDTO(playlist.getId(), playlist.getName());
    }


}
