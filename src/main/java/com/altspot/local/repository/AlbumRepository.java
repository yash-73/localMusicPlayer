package com.altspot.local.repository;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import com.altspot.local.payload.AlbumSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByNameAndPrimaryArtist(String albumName, Artist primaryArtist);

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

}
