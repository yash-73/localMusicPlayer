package com.altspot.local.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrackArtistMaintenanceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public long countByArtistId(Long artistId) {
        Object result = entityManager
                .createNativeQuery("""
                SELECT COUNT(*)
                FROM track_artist
                WHERE artist_id = :artistId
            """)
                .setParameter("artistId", artistId)
                .getSingleResult();

        return ((Number) result).longValue();
    }
}
