package com.altspot.local.repository;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByNameAndPrimaryArtist(String albumName, Artist primaryArtist);

}
