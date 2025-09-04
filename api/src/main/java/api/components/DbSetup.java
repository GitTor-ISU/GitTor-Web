package api.components;

import java.util.HashSet;
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
@Component("DbSetup")
public class DbSetup {
    public static final String AUTHORITY_READ = "AUTHORITY_READ";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";

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
        Set<Authority> authorities = new HashSet<>();
        for (String authorityName : authorityNames()) {
            authorities.add(authorityService.find(authorityName)
                .orElseGet(() -> authorityService.save(Authority.builder().authority(authorityName).build())));
        }

        // Setup Roles
        Role adminRole = roleService.find(RoleService.ADMIN_ROLE_NAME)
            .orElseGet(() -> roleService.save(
                Role.builder()
                    .name(RoleService.ADMIN_ROLE_NAME)
                    .authorities(authorities)
                    .build()
            ));
        Role userRole = roleService.find(RoleService.USER_ROLE_NAME)
            .orElseGet(() -> roleService.save(Role.builder().name(RoleService.USER_ROLE_NAME).build()));

        // Setup Admin
        userService.find("admin")
            .orElseGet(() -> userService.save(
                User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole, userRole))
                    .build()
            ));
    }

    /**
     * All authority names used in the system.
     *
     * @return {@link Set} of {@link String}
     */
    public Set<String> authorityNames() {
        return Set.of(
            AUTHORITY_READ,
            ROLE_READ,
            ROLE_WRITE,
            USER_READ,
            USER_WRITE
        );
    }
}
