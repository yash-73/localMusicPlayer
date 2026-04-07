package com.altspot.local.repository;

import com.altspot.local.model.PlaylistItem;
import com.altspot.local.payload.PlaylistItemSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    @Query("""
    select
        pl.id as playlistItemId,
        pl.position as position,
        t.name as trackName,
        t.id as trackId,
        t.durationSeconds as durationSeconds,
        a.id as albumId,
        a.name as albumName
    from PlaylistItem pl
    join pl.track t
    join t.album a
    where pl.playlist.id = :playlistId
    order by pl.position asc
""")
    List<PlaylistItemSummary> findAllSummariesByPlaylistId(Long playlistId);

    List<PlaylistItem> findAllByPlaylist_Id(Long playlistId);

    @Query("select max(pi.position) from PlaylistItem pi where pi.playlist.id = :playlistId")
    Optional<Long> findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("""
    select
    pl.id as playlistItemId,
    pl.position as position,
    t.name as trackName,
    t.id as trackId,
    t.durationSeconds as durationSeconds,
    a.id as albumId,
    a.name as albumName
    from PlaylistItem pl
    join pl.track t
    join t.album a
    where pl.id = :playlistItemId
""")
    PlaylistItemSummary findSummaryByPlaylistItemId(@Param("playlistItemId")Long playlistItemId);

    @Query("""
    select pl from PlaylistItem pl where pl.id = :playlistItemId and pl.playlist.id = :playlistId
""")
    Optional<PlaylistItem> findByPlaylistIdAndPlaylistItemId(@Param("playlistId") Long playlistId,@Param("playlistItemId") Long playlistItemId);


    @Query("""
    select
    pl.position as position
    from PlaylistItem pl
    where pl.id = :playlistItemId
    and pl.playlist.id = :playlistId
""")
    Optional<Long> findPositionByPlaylistIdAndPlaylistItemId(@Param("playlistId") Long playlistId, @Param("playlistItemId") Long playlistItemId);

    List<PlaylistItem> findAllByPlaylist_IdOrderByPositionAsc(Long playlistId);


}
