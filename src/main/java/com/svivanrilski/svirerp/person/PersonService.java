package com.svivanrilski.svirerp.person;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.svivanrilski.svirerp.common.ResourceNotFoundException;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository repo;

    public Page<Person> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Person findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person", id));
    }

    public Person findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with email: " + email));
    }

    /** Non-throwing lookup, for find-or-create flows (e.g. bulk member import). */
    public Optional<Person> findByEmailIfExists(String email) {
        return repo.findByEmail(email);
    }

    @Transactional
    public Person create(Person person) {
        if (person.getEmail() != null && repo.existsByEmail(person.getEmail())) {
            throw new IllegalArgumentException("A person with email '" + person.getEmail() + "' already exists");
        }
        return repo.save(person);
    }

    /**
     * Fills in only currently-null fields on an existing Person from {@code source} — never
     * overwrites data already on file. Used by find-or-create import flows (e.g. Zeffy) where the
     * imported row shouldn't clobber contact info a person may have updated through other means.
     */
    @Transactional
    public Person fillBlankFields(UUID id, Person source) {
        Person existing = findById(id);
        if (existing.getPhone() == null) existing.setPhone(source.getPhone());
        if (existing.getAddressLine1() == null) existing.setAddressLine1(source.getAddressLine1());
        if (existing.getCity() == null) existing.setCity(source.getCity());
        if (existing.getState() == null) existing.setState(source.getState());
        if (existing.getZip() == null) existing.setZip(source.getZip());
        if (existing.getDateOfBirth() == null) existing.setDateOfBirth(source.getDateOfBirth());
        return repo.save(existing);
    }

    @Transactional
    public Person update(UUID id, Person patch) {
        Person existing = findById(id);
        existing.setFirstName(patch.getFirstName());
        existing.setLastName(patch.getLastName());
        existing.setEmail(patch.getEmail());
        existing.setPhone(patch.getPhone());
        existing.setAddressLine1(patch.getAddressLine1());
        existing.setCity(patch.getCity());
        existing.setState(patch.getState());
        existing.setZip(patch.getZip());
        existing.setDateOfBirth(patch.getDateOfBirth());
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Person", id);
        repo.deleteById(id);
    }
}
