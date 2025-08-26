package api.components;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import api.entities.Authority;
import api.entities.Role;
import api.entities.User;
import api.services.AuthorityService;
import api.services.RoleService;
import api.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link DbSetup}.
 */
@Slf4j
@Component
public class DbSetup {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    @Value("${api.admin.username:admin}")
    private String adminUsername;
    @Value("${api.admin.password:password}")
    private String adminPassword;

    /**
     * Initialize the database with necessary fields.
     */
    @PostConstruct
    public void init() {
        log.info("Active profile: " + activeProfile);

        // Setup Authorities
        Authority authorityRead = authorityService.find("AUTHORITY_READ")
            .orElseGet(() -> authorityService.save(Authority.builder().authority("AUTHORITY_READ").build()));
        Authority userRead = authorityService.find("USER_READ")
            .orElseGet(() -> authorityService.save(Authority.builder().authority("USER_READ").build()));
        Authority userWrite = authorityService.find("USER_WRITE")
            .orElseGet(() -> authorityService.save(Authority.builder().authority("USER_WRITE").build()));
        Authority roleRead = authorityService.find("ROLE_READ")
            .orElseGet(() -> authorityService.save(Authority.builder().authority("ROLE_READ").build()));
        Authority roleWrite = authorityService.find("ROLE_WRITE")
            .orElseGet(() -> authorityService.save(Authority.builder().authority("ROLE_WRITE").build()));

        // Setup Roles
        Role adminRole = roleService.find("ADMIN")
            .orElseGet(() -> roleService.save(
                Role.builder()
                    .name("ADMIN")
                    .authorities(Set.of(authorityRead, userRead, userWrite, roleRead, roleWrite))
                    .build()
            ));
        Role userRole = roleService.find("USER")
            .orElseGet(() -> roleService.save(Role.builder().name("USER").build()));

        // Setup Users
        userService.find("admin")
            .orElseGet(() -> userService.save(
                User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole, userRole))
                    .build()
            ));
    }
}
