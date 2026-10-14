package com.wildwest.api.tour;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TourRepository
        extends JpaRepository<Tour, UUID>, JpaSpecificationExecutor<Tour> {

    Optional<Tour> findBySlug(String slug);

    @EntityGraph(attributePaths = {"destination", "styles"})
    @Query("select t from Tour t where t.slug = :slug")
    
    Optional<Tour> findBySlugWithBasicDetails(@Param("slug") String slug);
}