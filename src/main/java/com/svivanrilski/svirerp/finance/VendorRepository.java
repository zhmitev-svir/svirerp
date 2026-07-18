package com.svivanrilski.svirerp.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    @EntityGraph(attributePaths = {"org"})
    @Override
    Optional<Vendor> findById(UUID id);

    @EntityGraph(attributePaths = {"org"})
    Page<Vendor> findByOrgId(UUID orgId, Pageable pageable);

    boolean existsByOrgIdAndNameIgnoreCase(UUID orgId, String name);
}
