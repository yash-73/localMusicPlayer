package com.altspot.local.repository;

import com.altspot.local.model.Track;
import com.altspot.local.payload.AlbumTrackSummary;
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
        al.name as albumName,
        al.id as albumId
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
        al.name as albumName,
        al.id as albumId
    from Track t
    join t.album al
""")
    Page<TrackSummary> findAllProjected(Pageable pageable);



    @Query("""
select
  t.id as id,
  t.name as name,
  t.durationSeconds as durationSeconds,
  t.albumPosition as albumPosition,
  al.name as albumName,
  al.id as albumId
from Track t
join t.album  al
where t.album.id = :albumId
""")
    List<AlbumTrackSummary> findAllByAlbumId(@Param("albumId") Long albumId);


    @Query("""
select
  t.id as id,
  t.name as name,
  t.durationSeconds as durationSeconds,
  al.id as albumId,
  al.name as albumName
from Track t
join t.album al
where lower(t.name) like concat('%' ,  :keyword, '%')
""")
    List<TrackSummary> searchByPrefix(@Param("keyword") String keyword);


    @Query("""
        select
            distinct t.id as id,
            t.name as name,
            t.durationSeconds as durationSeconds,
            al.name as albumName,
            al.id as albumId
            from Track t
            join t.album al
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
        select t.id as trackId,
           a.id as artistId,
           a.name as artistName
    from Track t
    join t.artists a
    where t.id in :trackIds
""")
    List<TrackArtistFlatRow> findArtistsByTrackIds(@Param("trackIds")List<Long> trackIds);

    @Query(
            """
           select
               t.id as id,
               t.name as name,
               t.durationSeconds as durationSeconds,
               al.name as albumName,
               al.id as albumId
           from Track t
           join t.album al
           join t.artists a
           where a.id = :artistId
           and al.primaryArtist.id <> :artistId
    """
    )
    List<TrackSummary> findAllSinglesByArtistId(@Param("artistId") Long artistId);



}
