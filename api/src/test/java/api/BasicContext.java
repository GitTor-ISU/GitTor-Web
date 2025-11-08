package api;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

import api.dtos.AuthenticationDto;
import api.entities.Authority;
import api.entities.Role;
import api.entities.User;
import api.services.TokenService;
import net.jqwik.api.Arbitraries;

/**
 * Baseline configurations for all controller tests.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = """
        api.s3.max=2048
        api.s3.avatar.max=1024
    """
)
public abstract class BasicContext {
    @Autowired
    private TokenService tokenService;

    @LocalServerPort
    private int port;
    private static final String HOSTNAME = "localhost";

    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected String url;

    @MockitoBean
    protected Clock clock;
    protected ZoneId zone = ZoneId.of("UTC");
    protected Instant instant = Instant.EPOCH;

    @Value("${api.admin.email:admin@gittor}")
    protected String adminEmail;
    @Value("${api.admin.username:admin}")
    protected String adminUsername;
    @Value("${api.admin.password:password}")
    protected String adminPassword;
    protected AuthenticationDto adminAuth;

    protected static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .defaultNullInjectGenerator(context -> 0.0)
            .plugin(new JakartaValidationPlugin())
            .plugin(new SimpleValueJqwikPlugin())
            .build();

    @BeforeEach
    public void setup() {
        url = "http://" + HOSTNAME + ":" + port;

        when(clock.getZone()).thenAnswer(inv -> zone);
        when(clock.instant()).thenAnswer(inv -> instant);

        adminAuth = tokenService.generateToken(
            new UsernamePasswordAuthenticationToken(adminUsername, null, Collections.emptyList())
        );
    }

    protected ArbitraryBuilder<Authority> getAuthorityBuilder() {
        return fixtureMonkey.giveMeBuilder(Authority.class)
                .setNull(javaGetter(Authority::getId))
                .set(
                    javaGetter(Authority::getAuthority),
                    Arbitraries.strings().ofMinLength(10).ofMaxLength(255)
                );
    }

    protected ArbitraryBuilder<Role> getRoleBuilder() {
        return fixtureMonkey.giveMeBuilder(Role.class)
                .setNull(javaGetter(Role::getId))
                .setNull(javaGetter(Role::getAuthorities))
                .set(
                    javaGetter(Role::getName),
                    Arbitraries.strings().ofMinLength(10).ofMaxLength(255)
                );
    }

    protected ArbitraryBuilder<User> getUserBuilder() {
        return fixtureMonkey.giveMeBuilder(User.class)
                .setNull(javaGetter(User::getAvatar))
                .setNull(javaGetter(User::getId))
                .set(
                    javaGetter(User::getEmail),
                    Arbitraries.strings().ofMinLength(10).ofMaxLength(255)
                )
                .set(
                    javaGetter(User::getUsername),
                    Arbitraries.strings().ofMinLength(10).ofMaxLength(255)
                );
    }
}
