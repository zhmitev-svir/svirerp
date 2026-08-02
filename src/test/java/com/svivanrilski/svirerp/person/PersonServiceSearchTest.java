package com.svivanrilski.svirerp.person;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the autocomplete search's allow-list guard — the whole point of PersonService#search is
 * that a client-supplied field name is never trusted without being checked first (see
 * SearchSpecifications). All collaborators are mocked; this doesn't stand up a Spring context.
 */
@ExtendWith(MockitoExtension.class)
class PersonServiceSearchTest {

    @Mock private PersonRepository repo;

    @InjectMocks
    private PersonService service;

    @Test
    @SuppressWarnings("unchecked")
    void search_allowedField_returnsMatches() {
        Person match = new Person();
        match.setFirstName("John");
        Page<Person> page = new PageImpl<>(List.of(match));
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<Person> result = service.search("firstName", "Jo");

        assertThat(result).containsExactly(match);
    }

    @Test
    void search_unlistedField_rejectsWithoutHittingRepo() {
        assertThatThrownBy(() -> service.search("email", "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not searchable");

        verifyNoInteractions(repo);
    }

    @Test
    void search_blankQuery_shortCircuitsWithoutHittingRepo() {
        List<Person> result = service.search("firstName", "  ");

        assertThat(result).isEmpty();
        verifyNoInteractions(repo);
    }
}
