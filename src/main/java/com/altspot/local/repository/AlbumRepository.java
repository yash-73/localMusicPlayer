package com.altspot.local.repository;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import com.altspot.local.payload.AlbumSummary;
import com.altspot.local.payload.TrackSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {


    Optional<Album> findByNameAndPrimaryArtist_Id(
            @Param("albumName")String albumName,
            @Param("primaryArtistId") Long primaryArtistId
    );

    @Query("""
    select
     al.id as id,
     al.name as name,
     pa.id as primaryArtistId,
     pa.name as primaryArtistName
     from Album al
     join al.primaryArtist pa
""")
    Page<AlbumSummary> findAllProjectedBy(Pageable pageDetails);

    @Query("""
    select
        al.id as id,
        al.name as name,
        pa.id as primaryArtist,
        pa.name as primaryArtistName
        from Album al
        join al.primaryArtist pa
        where al.id = :albumId
    """)
    Optional<AlbumSummary> findAlbumProjectedBy(Long albumId);



    @Query("""
    select
      al.id as id,
      al.name as name,
      al.primaryArtist.id as primaryArtistId,
      al.primaryArtist.name as primaryArtistName
    from Album al
    where al.primaryArtist.id = :artistId
""")
    List<AlbumSummary> findAllByArtistId(@Param("artistId") Long artistId);

    @Query("""
    select
      al.id as id,
      al.name as name,
      al.primaryArtist.id as primaryArtistId,
      al.primaryArtist.name as primaryArtsitName
    from Album al
    where lower(al.name) like concat('%' ,  :keyword, '%')
""")
    List<AlbumSummary> searchByPrefix(@Param("keyword") String keyword);


    Optional<Album> findByNameAndPrimaryArtist_IdAndReleaseYear(
            String name,
            Long primaryArtistId,
            Integer releaseYear
    );


}
