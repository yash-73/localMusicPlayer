package com.altspot.local.repository;

import com.altspot.local.model.Track;
import com.altspot.local.payload.ArtistSummary;
import com.altspot.local.payload.TrackArtistFlatRow;
import com.altspot.local.payload.TrackSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByFilePath(String filePath);

    @Query("SELECT t.filePath FROM Track t")
    Set<String> findAllFilePaths();

    int countByAlbum_Id(Long albumId);

    @Query("""
    select
        t.id as id,
        t.name as name,
        t.durationSeconds as durationSeconds,
        al.name as albumName
            from Track t
            join t.album al
            where t.id = :id
    """)
    TrackSummary getTrackSummaryById(Long id);

    @Query("""
    select
        t.id as id,
        t.name as name,
        t.durationSeconds as durationSeconds,
        al.name as albumName
    from Track t
    join t.album al
""")
    Page<TrackSummary> findAllProjected(Pageable pageable);



    @Query("""
select
  t.id as id,
  t.name as name,
  t.durationSeconds as durationSeconds,
  al.name as albumName
from Track t
left join t.album  al
where t.album.id = :albumId
""")
    List<TrackSummary> findAllByAlbumId(@Param("albumId") Long albumId);


    @Query("""
select
  t.id as id,
  t.name as name,
  t.durationSeconds as durationSeconds
from Track t
where lower(t.name) like concat('%' ,  :keyword, '%')
""")
    List<TrackSummary> searchByPrefix(@Param("keyword") String keyword);


    @Query("""
        select
            t.id as id,
            t.name as name,
            t.durationSeconds as durationSeconds
            from Track t
            join t.artists a
            where a.id = :artistId
    """)
    List<TrackSummary> findAllByArtistId(Long artistId);


    @Query("""
    select
        a.id as artistId,
        a.name as artistName
    from Track t
    join t.artists a
    where t.id = :trackId
""")
    List<ArtistSummary> findArtistsByTrackId(@Param("trackId") Long trackId);


    @Query("""
        select ta.track.id as trackId,
           a.id as artistId,
           a.name as artistName
    from TrackArtist ta
    join ta.artist a
    where ta.track.id in :trackIds
""")
    List<TrackArtistFlatRow> findArtistsByTrackIds(List<Long> trackIds);



}
