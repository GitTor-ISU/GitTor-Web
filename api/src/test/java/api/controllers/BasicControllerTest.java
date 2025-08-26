package api.controllers;

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

import api.dtos.AuthenticationDto;
import api.services.TokenService;

/**
 * Baseline configurations for all controller tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BasicControllerTest {
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

    @Value("${api.admin.username:admin}")
    private String adminUsername;
    @Value("${api.admin.password:password}")
    private String adminPassword;
    protected AuthenticationDto adminAuth;

    @BeforeEach
    public void setup() {
        url = "http://" + HOSTNAME + ":" + port;

        when(clock.getZone()).thenAnswer(inv -> zone);
        when(clock.instant()).thenAnswer(inv -> instant);

        adminAuth = tokenService.generateToken(
            new UsernamePasswordAuthenticationToken(adminUsername, null, Collections.emptyList())
        );
    }
}
