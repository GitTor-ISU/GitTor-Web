package api.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.entities.Role;
import api.entities.User;
import api.exceptions.EntityNotFoundException;
import api.repositories.RoleRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link RoleService}.
 */
@Slf4j
@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserService userService;

    /**
     * Find role.
     *
     * @param id Role id
     * @return {@link Optional} {@link Role}
     */
    @Transactional(readOnly = true)
    public Optional<Role> find(int id) {
        return roleRepository.findById(id);
    }

    /**
     * Find role.
     *
     * @param role Role name
     * @return {@link Optional} {@link Role}
     */
    @Transactional(readOnly = true)
    public Optional<Role> find(String role) {
        return roleRepository.findByName(role);
    }

    /**
     * Check if role exists.
     *
     * @param id Role id
     * @return true if role exists
     */
    @Transactional(readOnly = true)
    public boolean exists(int id) {
        return roleRepository.existsById(id);
    }

    /**
     * Check if role exists.
     *
     * @param role Role name
     * @return true if role exists
     */
    @Transactional(readOnly = true)
    public boolean exists(String role) {
        return find(role).isPresent();
    }

    /**
     * Get all roles.
     *
     * @return All roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    /**
     * Get roles.
     *
     * @param ids Role ids
     * @return {@link List} of {@link Role}
     */
    @Transactional(readOnly = true)
    public List<Role> get(Iterable<Integer> ids) {
        return roleRepository.findAllById(ids);
    }

    /**
     * Get role.
     *
     * @param id Role id
     * @return {@link Role}
     */
    @Transactional(readOnly = true)
    public Role get(int id) {
        return find(id)
            .orElseThrow(() -> EntityNotFoundException.fromRole(id));
    }

    /**
     * Get role.
     *
     * @param role Role name
     * @return {@link Role}
     */
    @Transactional(readOnly = true)
    public Role get(String role) {
        return find(role)
            .orElseThrow(() -> EntityNotFoundException.fromRole(role));
    }

    /**
     * Save role.
     *
     * @param role Role
     * @return Role
     */
    @Transactional
    public Role save(Role role) {
        Role saved = roleRepository.save(role);
        log.info("Role saved: " + role);
        return saved;
    }

    /**
     * Detaches this role from all users.
     *
     * @param role Role
     */
    @Transactional
    public void detachFromUsers(Role role) {
        for (User user : userService.getAllContainingRole(role)) {
            Set<Role> roles = user.getRoles();
            roles.remove(role);
            user.setRoles(roles);
            userService.save(user);
        }
    }

    /**
     * Delete role.
     *
     * @param id Role id
     */
    @Transactional
    public void delete(int id) {
        delete(get(id));
    }

    /**
     * Delete role.
     *
     * @param role Role
     */
    @Transactional
    public void delete(Role role) {
        detachFromUsers(role);
        roleRepository.delete(role);
        log.info("Role deleted: " + role);
    }
}
