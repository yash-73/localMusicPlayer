package com.altspot.local.repository;

import com.altspot.local.model.Track;
import jdk.jfr.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByFilePath(String filePath);

    @Query("SELECT t.filePath FROM Track t")
    Set<String> findAllFilePaths();

    void deleteByFilePath(String filePath);


}
