package com.altspot.local.repository;

import com.altspot.local.model.Playlist;
import com.altspot.local.payload.PlaylistMetaSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {


    @Query("""
        select pl.id as playlistId,
        pl.name as playlistName
        from Playlist pl
        where pl.user.id = :userId
""")
    List<PlaylistMetaSummary> findByUserId(Long userId);


    @Query("""
        select pl.id as playlistId,
        pl.name as playlistName
        from Playlist pl
        where pl.id = :playlistId
""")
    PlaylistMetaSummary findByPlaylistId(Long playlistId);


    @Query("""
        select
        pl.id as playlistId,
        pl.name as playlistName
        from Playlist pl
        where pl.name = :playlistName and pl.user.id = :userId
    """)
    PlaylistMetaSummary findByPlaylistNameAndUserId( @Param("playlistName") String playlistName, @Param("userId") Long userId);

    @Query(
            """
            select pl
            from Playlist pl
            where pl.id = :playlistId
"""
    )
    Playlist findPlaylistByPlaylistId(Long playlistId);

}
