package api.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.entities.User;
import api.exceptions.EntityNotFoundException;
import api.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link UserService}.
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Load user with authorities by username.
     *
     * @param username Username
     * @return {@link User}
     */
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithAuthorities(username)
            .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' found"));
        return user;
    }

    /**
     * Find user.
     *
     * @param id User id
     * @return {@link Optional} {@link User}
     */
    @Transactional(readOnly = true)
    public Optional<User> find(int id) {
        return userRepository.findById(id);
    }

    /**
     * Find user.
     *
     * @param username Username
     * @return {@link Optional} {@link User}
     */
    @Transactional(readOnly = true)
    public Optional<User> find(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Check if user exists.
     *
     * @param id User id
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean exists(int id) {
        return userRepository.existsById(id);
    }

    /**
     * Check if user exists.
     *
     * @param username Username
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return find(username).isPresent();
    }

    /**
     * Get all users.
     *
     * @param pageable {@link Pageable}
     * @return User ids.
     */
    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get user.
     *
     * @param id User id
     * @return {@link User}
     */
    @Transactional(readOnly = true)
    public User get(int id) {
        User user = find(id)
            .orElseThrow(() -> new EntityNotFoundException("User " + id + " not found."));
        return user;
    }

    /**
     * Get user.
     *
     * @param username Username
     * @return {@link User}
     */
    @Transactional(readOnly = true)
    public User get(String username) {
        User user = find(username)
            .orElseThrow(() -> new EntityNotFoundException("User '" + username + "' not found."));
        return user;
    }

    /**
     * Save user.
     *
     * @param user User
     * @return User
     */
    @Transactional
    public User save(User user) {
        User saved = userRepository.save(user);
        log.info("User saved: " + user);
        return saved;
    }

    /**
     * Delete user.
     *
     * @param id User id
     */
    @Transactional
    public void delete(int id) {
        if (!exists(id)) {
            throw new EntityNotFoundException("User " + id + " not found.");
        }
        userRepository.deleteById(id);
        log.info("User deleted: " + id);
    }

    /**
     * Delete user.
     *
     * @param user User
     */
    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
        log.info("User deleted: " + user);
    }
}
