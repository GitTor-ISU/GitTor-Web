package api.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userService.exists(registerDto.getEmail())) {
            throw DuplicateEntityException.fromEmail(registerDto.getEmail());
        }
        if (userService.exists(registerDto.getUsername())) {
            throw DuplicateEntityException.fromUsername(registerDto.getUsername());
        }

        Role userRole = roleService.get(RoleService.USER_ROLE_NAME);

        User user = User.builder()
            .email(registerDto.getEmail())
            .username(registerDto.getUsername())
            .password(encoder.encode(registerDto.getPassword()))
            .roles(Set.of(userRole))
            .build();

        return userService.save(user);
    }
}
