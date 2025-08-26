package api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.entities.Authority;
import api.exceptions.EntityNotFoundException;
import api.repositories.AuthorityRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link AuthorityService}.
 */
@Slf4j
@Service
public class AuthorityService {
    @Autowired
    private AuthorityRepository authorityRepository;

    /**
     * Find authority.
     *
     * @param id Authority id
     * @return {@link Optional} {@link Authority}
     */
    @Transactional(readOnly = true)
    public Optional<Authority> find(int id) {
        return authorityRepository.findById(id);
    }

    /**
     * Find authority.
     *
     * @param authority Authority name
     * @return {@link Optional} {@link Authority}
     */
    @Transactional(readOnly = true)
    public Optional<Authority> find(String authority) {
        return authorityRepository.findByAuthority(authority);
    }


    /**
     * Get all authorities.
     *
     * @return All authorities
     */
    @Transactional(readOnly = true)
    public List<Authority> getAll() {
        return authorityRepository.findAll();
    }

    /**
     * Get authorities.
     *
     * @param ids Authority ids
     * @return {@link List} of {@link Authority}
     */
    @Transactional(readOnly = true)
    public List<Authority> get(Iterable<Integer> ids) {
        return authorityRepository.findAllById(ids);
    }

    /**
     * Get authority.
     *
     * @param id Authority id
     * @return {@link Authority}
     */
    @Transactional(readOnly = true)
    public Authority get(int id) {
        return find(id)
            .orElseThrow(() -> new EntityNotFoundException("Authority " + id + " not found."));
    }

    /**
     * Get authority.
     *
     * @param authority Authority name
     * @return {@link Authority}
     */
    @Transactional(readOnly = true)
    public Authority get(String authority) {
        return find(authority)
            .orElseThrow(() -> new EntityNotFoundException("Authority '" + authority + "' not found."));
    }

    /**
     * Save authority.
     *
     * @param authority Authority
     * @return Authority
     */
    @Transactional
    public Authority save(Authority authority) {
        Authority saved = authorityRepository.save(authority);
        log.info("Authority saved: " + authority);
        return saved;
    }
}
