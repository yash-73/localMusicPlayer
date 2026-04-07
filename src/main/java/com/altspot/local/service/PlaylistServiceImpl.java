package com.altspot.local.service;

import com.altspot.local.config.userdetails.UserDetailsImpl;
import com.altspot.local.exception.GeneralException;
import com.altspot.local.exception.NullParameterException;
import com.altspot.local.exception.ResourceNotFound;
import com.altspot.local.model.*;
import com.altspot.local.payload.*;
import com.altspot.local.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.sql.ast.tree.expression.Over;
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
    private final ArtistRepository artistRepository;

    public PlaylistServiceImpl(UserRepository userRepository,  PlaylistRepository playlistRepository,
                               PlaylistItemRepository playlistItemRepository , TrackRepository trackRepository,
                               ArtistRepository artistRepository) {
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
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

        List<PlaylistItemSummary> summaries = playlistItemRepository.findAllSummariesByPlaylistId(playlistId);

        Map<Long , Set<ArtistDTO>> artistMap = getArtistDTOsFromTrackSummaries(summaries);

        return summaries.stream().map(summary ->
                convertPlaylistItemSummaryToDTO(summary , artistMap.getOrDefault(summary.getTrackId() , Set.of()))
        ).toList();

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

    @Transactional
    @Override
    public PlaylistItemDTO addTrackToPlaylist(Long playlistId, Long trackId) {
        if(playlistId == null) throw new NullParameterException("playlistId is null");
        if(trackId == null) throw new NullParameterException("trackId is null");

        Playlist playlist = playlistRepository.findPlaylistByPlaylistId(playlistId);
        if(playlist == null) throw new ResourceNotFound("Playlist not found");

        Optional<Track> track = trackRepository.findById(trackId);
        if(track.isEmpty()) throw new ResourceNotFound("Track not found");

        Track currentTrack = track.get();

        PlaylistItem playlistItem = new PlaylistItem();

        playlistItem.setTrack(currentTrack);
        playlistItem.setPlaylist(playlist);

        //Setting the position
        long position;
        Optional<Long> lastPosition = playlistItemRepository.findMaxPositionByPlaylistId(playlistId);
        position = lastPosition.orElse(0L);
        position += 1000L;
        playlistItem.setPosition(position);

        //Save playlistItem
        PlaylistItem savedPlaylistItem = playlistItemRepository.save(playlistItem);

        Long id  = savedPlaylistItem.getId();
        PlaylistItemSummary summary = playlistItemRepository.findSummaryByPlaylistItemId(id);

        List<ArtistSummary> artistSummaries = trackRepository.findArtistsByTrackId(trackId);

        Set<ArtistDTO> artistDTOs = artistSummaries.stream()
                .map(artistSummary ->
                        new ArtistDTO(artistSummary.getArtistId() , artistSummary.getArtistName()))
                .collect(Collectors.toSet());

        return convertPlaylistItemSummaryToDTO(summary , artistDTOs);
    }

    @Transactional
    @Override
    public PlaylistItemDTO removeTrackFromPlaylist(Long playlistId, Long playlistItemId) {
        if(playlistId == null) throw new NullParameterException("playlistId is null");
        if(playlistItemId == null) throw new NullParameterException("trackId is null");

        PlaylistItem playlistItem = playlistItemRepository.findByPlaylistIdAndPlaylistItemId(playlistId , playlistItemId)
                .orElseThrow(() -> new ResourceNotFound("Playlist Item not found"));

        List<ArtistSummary> artistSummaries = trackRepository.findArtistsByTrackId(playlistItem.getTrack().getId());
        Set<ArtistDTO> artistDTOs = artistSummaries.stream()
                .map(artistSummary ->
                        new ArtistDTO(artistSummary.getArtistId() , artistSummary.getArtistName()))
                .collect(Collectors.toSet());

        PlaylistItemDTO deletedPlaylistItemDTO =  convertPlaylistItemToPlaylistItemDTO(playlistItem, artistDTOs);

        //Delete method
        playlistItemRepository.delete(playlistItem);

        return deletedPlaylistItemDTO;
    }
    @Transactional
    @Override
    public void reorderPlaylistItem(Long playlistId,
                                    Long playlistItemId,
                                    Long previousId,
                                    Long nextId) {

        if (playlistId == null) throw new NullParameterException("playlistId is null");
        if (playlistItemId == null) throw new NullParameterException("playlistItemId is null");

        PlaylistItem item = playlistItemRepository
                .findByPlaylistIdAndPlaylistItemId(playlistId, playlistItemId)
                .orElseThrow(() -> new ResourceNotFound("Item not found"));

        Long prevPos = null;
        Long nextPos = null;

        if (previousId != -1) {
            prevPos = playlistItemRepository
                    .findPositionByPlaylistIdAndPlaylistItemId(playlistId, previousId)
                    .orElseThrow(() -> new ResourceNotFound("Previous not found"));
        }

        if (nextId != -1) {
            nextPos = playlistItemRepository
                    .findPositionByPlaylistIdAndPlaylistItemId(playlistId, nextId)
                    .orElseThrow(() -> new ResourceNotFound("Next not found"));
        }

        // Validate logical ordering
        if (prevPos != null && nextPos != null && prevPos >= nextPos) {
            throw new IllegalArgumentException("Invalid ordering state: prev >= next");
        }

        // ---- FIRST POSITION ----
        if (previousId == -1) {

            if (nextPos == null)
                throw new IllegalStateException("Next item required for first insertion");

            long candidate = nextPos - 1000;

            if (candidate >= 0) {
                item.setPosition(candidate);
                playlistItemRepository.save(item);
            } else {
                reindexWithInsertion(playlistId, item, previousId, nextId);
            }

            return;
        }

        // ---- LAST POSITION ----
        if (nextId == -1) {

            if (prevPos == null)
                throw new IllegalStateException("Previous item required for last insertion");

            item.setPosition(prevPos + 1000);
            playlistItemRepository.save(item);
            return;
        }

        // ---- BETWEEN TWO ITEMS ----
        long gap = nextPos - prevPos;

        if (gap > 1) {
            long newPos = prevPos + gap / 2;
            item.setPosition(newPos);
            playlistItemRepository.save(item);
        } else {
            reindexWithInsertion(playlistId, item, previousId, nextId);
        }
    }

    private void reindexWithInsertion(Long playlistId,
                                      PlaylistItem movingItem,
                                      Long previousId,
                                      Long nextId) {

        List<PlaylistItem> items =
                playlistItemRepository
                        .findAllByPlaylist_IdOrderByPositionAsc(playlistId);

        // Remove moving item
        items.removeIf(i -> i.getId().equals(movingItem.getId()));

        int insertIndex;

        if (previousId == -1) {
            insertIndex = 0;
        }
        else if (nextId == -1) {
            insertIndex = items.size();
        }
        else {
            insertIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(previousId)) {
                    insertIndex = i + 1;
                    break;
                }
            }

            if (insertIndex == -1) {
                throw new IllegalStateException("Previous item not found in playlist during reindex");
            }
        }

        items.add(insertIndex, movingItem);

        long position = 1000L;

        for (PlaylistItem item : items) {
            item.setPosition(position);
            position += 1000L;
        }

        playlistItemRepository.saveAll(items);
    }

    public PlaylistItemDTO convertPlaylistItemToPlaylistItemDTO(PlaylistItem playlistItem , Set<ArtistDTO> artistDTOs) {
        PlaylistItemDTO playlistItemDTO = new PlaylistItemDTO();
        playlistItemDTO.setPlaylistItemId(playlistItem.getId());
        playlistItemDTO.setPosition(playlistItem.getPosition());
        playlistItemDTO.setTrackId(playlistItem.getTrack().getId());
        playlistItemDTO.setTrackName(playlistItem.getTrack().getName());
        playlistItemDTO.setDurationSeconds(playlistItem.getTrack().getDurationSeconds());
        playlistItemDTO.setAlbumId(playlistItem.getTrack().getAlbum().getId());
        playlistItemDTO.setAlbumName(playlistItem.getTrack().getAlbum().getName());
        playlistItemDTO.setArtists(artistDTOs);
        return playlistItemDTO;
    }

    public PlaylistItemDTO convertPlaylistItemSummaryToDTO(PlaylistItemSummary playlistItemSummary , Set<ArtistDTO> artistDTOs) {
        PlaylistItemDTO playlistItemDTO = new PlaylistItemDTO();
        playlistItemDTO.setPlaylistItemId(playlistItemSummary.getPlaylistItemId());
        playlistItemDTO.setPosition(playlistItemSummary.getPosition());
        playlistItemDTO.setTrackId(playlistItemSummary.getTrackId());
        playlistItemDTO.setTrackName(playlistItemSummary.getTrackName());
        playlistItemDTO.setDurationSeconds(playlistItemSummary.getDurationSeconds());
        playlistItemDTO.setAlbumId(playlistItemSummary.getAlbumId());
        playlistItemDTO.setAlbumName(playlistItemSummary.getAlbumName());
        playlistItemDTO.setArtists(artistDTOs);
        return playlistItemDTO;

    }

    Map<Long , Set<ArtistDTO>> getArtistDTOsFromTrackSummaries(List<PlaylistItemSummary> summaries){
        List<Long> trackIds = summaries.stream().map(PlaylistItemSummary::getTrackId).toList();

        List<TrackArtistFlatRow> trackArtistFlatRows = trackRepository.findArtistsByTrackIds(trackIds);

        Map<Long, Set<ArtistDTO>> artistMap = new HashMap<>();

        for (var row : trackArtistFlatRows) {
            artistMap
                    .computeIfAbsent(row.getTrackId(), k -> new HashSet<>())
                    .add(new ArtistDTO(row.getArtistId(), row.getArtistName()));
        }
        return artistMap;
    }






}
