package com.altspot.local.repository;

import com.altspot.local.model.PlaylistItem;
import com.altspot.local.payload.PlaylistMetaSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistItem, Long> {


    @Query("""
        select pl.id as playlistId,
        pl.name as playlistName
        from Playlist pl
        where pl.user.id = :userId
""")
    List<PlaylistMetaSummary> findByUserId(Long userId);
}
