package org.svir.svirerp.person;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.svir.svirerp.common.ResourceNotFoundException;

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

    @Transactional
    public Person create(Person person) {
        if (person.getEmail() != null && repo.existsByEmail(person.getEmail())) {
            throw new IllegalArgumentException("A person with email '" + person.getEmail() + "' already exists");
        }
        return repo.save(person);
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
