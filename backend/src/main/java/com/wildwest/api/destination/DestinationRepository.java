package com.wildwest.api.destination;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    List<Destination> findAllByOrderByDisplayOrderAsc();

    Optional<Destination> findBySlug(String slug);
}
