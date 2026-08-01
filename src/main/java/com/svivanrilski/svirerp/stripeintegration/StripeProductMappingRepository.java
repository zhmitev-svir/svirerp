package com.svivanrilski.svirerp.stripeintegration;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StripeProductMappingRepository extends JpaRepository<StripeProductMapping, UUID> {

    @EntityGraph(attributePaths = {"org", "fund", "categoryAccount"})
    List<StripeProductMapping> findByOrgId(UUID orgId);

    @EntityGraph(attributePaths = {"org", "fund", "categoryAccount"})
    Optional<StripeProductMapping> findByOrgIdAndStripePriceId(UUID orgId, String stripePriceId);
}
