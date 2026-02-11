package com.altspot.local.service;

import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.User;
import com.altspot.local.payload.PlaylistItemDTO;
import com.altspot.local.payload.PlaylistItemSummary;
import com.altspot.local.payload.PlaylistMeta;
import com.altspot.local.payload.PlaylistMetaSummary;
import com.altspot.local.repository.PlaylistItemRepository;
import com.altspot.local.repository.PlaylistRepository;
import com.altspot.local.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlaylistServiceImpl implements PlaylistService {


    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;

    public PlaylistServiceImpl(UserRepository userRepository,  PlaylistRepository playlistRepository,
                               PlaylistItemRepository playlistItemRepository) {
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
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


    //Get list of tracks from a playlist
    @Override
    public List<PlaylistItemDTO> getAllPlaylistItemsForPlaylist(Long playlistId) {
        if(!playlistRepository.existsById(playlistId)) throw new ResourceNotFound("Playlist not found");

        List<PlaylistItemSummary> summaries = playlistItemRepository.findAllByPlaylistId(playlistId);
        return summaries.stream().map(summary -> {
            PlaylistItemDTO playlistItemDTO = new PlaylistItemDTO();
            playlistItemDTO.setPlaylistItemId(summary.getPlaylistItemId());
            playlistItemDTO.setPosition(summary.getPosition());
            playlistItemDTO.setTrackId(summary.getTrackId());
            playlistItemDTO.setTrackName(summary.getTrackName());

            return playlistItemDTO;
        }).toList();

    }
}
