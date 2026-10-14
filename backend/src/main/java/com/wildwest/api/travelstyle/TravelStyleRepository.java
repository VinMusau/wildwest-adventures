package com.wildwest.api.travelstyle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TravelStyleRepository extends JpaRepository<TravelStyle, UUID> {

    List<TravelStyle> findAllByOrderByDisplayOrderAsc();

    Optional<TravelStyle> findBySlug(String slug);
}