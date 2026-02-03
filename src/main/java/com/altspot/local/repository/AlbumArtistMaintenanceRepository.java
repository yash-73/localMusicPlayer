package com.altspot.local.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class AlbumArtistMaintenanceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void rebuildAlbumArtist() {

        entityManager.createNativeQuery("TRUNCATE TABLE album_artist")
                .executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO album_artist (album_id, artist_id)
            SELECT DISTINCT
                t.album_id,
                ta.artist_id
            FROM track t
            JOIN track_artist ta ON ta.track_id = t.track_id
        """).executeUpdate();
    }
}
