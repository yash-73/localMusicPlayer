package com.altspot.local.repository;

import com.altspot.local.model.PlaylistItem;
import com.altspot.local.payload.PlaylistItemSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    @Query("""
            select
            pl.id as playlistItemId,
            pl.position as position,
            pl.track.name as trackName,
            pl.track.id as trackId,
            pl.track.durationSeconds as durationSeconds
            from PlaylistItem pl
            where pl.playlist.id = :playlistId""")
    List<PlaylistItemSummary> findAllByPlaylistId(Long playlistId);
}
