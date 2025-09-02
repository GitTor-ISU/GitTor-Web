package api.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import api.dtos.RegisterDto;
import api.entities.Role;
import api.entities.User;
import api.exceptions.DuplicateEntityException;

/**
 * {@link AuthenticationService}.
 */
@Service
public class AuthenticationService {
    @Autowired
    public UserService userService;
    @Autowired
    public RoleService roleService;
    @Autowired
    private PasswordEncoder encoder;

    /**
     * Register a new user.
     *
     * @param registerDto Register information
     * @return {@link User}
     */
    @Transactional
    public User register(RegisterDto registerDto) {
        if (!StringUtils.hasText(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        if (!StringUtils.hasText(registerDto.getPassword())) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        if (userService.exists(registerDto.getUsername())) {
            throw new DuplicateEntityException("Username '" + registerDto.getUsername() + "' already exists.");
        }

        Role userRole = roleService.get("USER");

        User user = User.builder()
            .username(registerDto.getUsername())
            .password(encoder.encode(registerDto.getPassword()))
            .roles(Set.of(userRole))
            .build();

        return userService.save(user);
    }
}
