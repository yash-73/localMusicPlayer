package com.altspot.local.repository;

import com.altspot.local.model.Track;
import com.altspot.local.payload.AlbumSummary;
import com.altspot.local.payload.TrackSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByFilePath(String filePath);

    @Query("SELECT t.filePath FROM Track t")
    Set<String> findAllFilePaths();

    void deleteByFilePath(String filePath);

    Page<TrackSummary> findAllProjectedBy(Pageable pageable);

    @Query(
            value = """
        select
            t.album as album,
            min(t.artist) as artist,
            count(t.id) as trackCount
        from Track t
        group by t.album
        order by t.album
    """,
            countQuery = """
        select count(distinct t.album)
        from Track t
    """
    )
    Page<AlbumSummary> findAlbumSummaries(Pageable pageable);




}
