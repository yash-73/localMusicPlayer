package com.altspot.local.repository;

import com.altspot.local.model.Artist;
import com.altspot.local.payload.AlbumSummary;
import com.altspot.local.payload.ArtistSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByName(String name);

    @Query("""
    select ar.id as artistId,
        ar.name as artistName
            from Artist ar
    """)
    Page<ArtistSummary> findAllProjectedBy(Pageable pageDetails);

    @Query("""
    select
      ar.id as id,
      ar.name as name
    from Artist ar
    where lower(ar.name) like concat('%' ,  :keyword, '%')
""")
    List<ArtistSummary> searchByPrefix(@Param("keyword") String keyword);

}
