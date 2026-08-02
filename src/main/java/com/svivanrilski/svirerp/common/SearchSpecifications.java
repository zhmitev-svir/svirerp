package com.svivanrilski.svirerp.common;

import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA {@link Specification} builder for autocomplete-style "contains" search. Never
 * accepts a field name it wasn't told is safe — callers must validate the requested field against
 * their own per-entity allow-list before calling {@link #contains}, so a client can never search an
 * arbitrary or sensitive column just by asking for it. See any domain Service's `search(field, query)`
 * method (e.g. `PersonService`) for the copy-pasteable pattern this is meant to support everywhere.
 */
public final class SearchSpecifications {

    private SearchSpecifications() {
    }

    public static <T> Specification<T> contains(String field, String value) {
        String needle = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), needle);
    }
}
