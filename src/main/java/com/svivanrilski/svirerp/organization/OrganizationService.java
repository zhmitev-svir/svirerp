package com.svivanrilski.svirerp.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.svivanrilski.svirerp.common.ResourceNotFoundException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository repo;

    public Page<Organization> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Organization findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }

    /**
     * This is a single-org app (see {@link Organization}'s own doc comment) — there's no org
     * picker, so a server-initiated flow with no org-scoped path parameter to work from (e.g. a
     * Stripe webhook) resolves whichever single organization exists. Mirrors the frontend's
     * OrgContextService#ensureOrgId.
     */
    public Organization getSingleOrganization() {
        return repo.findAll(Pageable.ofSize(1)).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No organization exists yet"));
    }

    @Transactional
    public Organization create(Organization org) {
        return repo.save(org);
    }

    @Transactional
    public Organization update(UUID id, Organization patch) {
        Organization existing = findById(id);
        existing.setName(patch.getName());
        existing.setLegalName(patch.getLegalName());
        existing.setTaxIdEin(patch.getTaxIdEin());
        existing.setNonprofitType(patch.getNonprofitType());
        existing.setMissionStatement(patch.getMissionStatement());
        existing.setAddressLine1(patch.getAddressLine1());
        existing.setAddressLine2(patch.getAddressLine2());
        existing.setCity(patch.getCity());
        existing.setState(patch.getState());
        existing.setZip(patch.getZip());
        existing.setCountry(patch.getCountry());
        existing.setPhone(patch.getPhone());
        existing.setEmail(patch.getEmail());
        existing.setWebsite(patch.getWebsite());
        existing.setFoundedDate(patch.getFoundedDate());
        existing.setFiscalYearStart(patch.getFiscalYearStart());
        existing.setLogoUrl(patch.getLogoUrl());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Organization", id);
        repo.deleteById(id);
    }
}
