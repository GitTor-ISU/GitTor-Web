package api.services;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

import api.entities.Authority;
import api.entities.Role;
import api.entities.User;
import lombok.Getter;
import net.jqwik.api.Arbitraries;

/**
 * {@link FixtureService}.
 */
@Service
public class FixtureService {
    @Getter
    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .defaultNullInjectGenerator(context -> 0.0)
            .plugin(new JakartaValidationPlugin())
            .plugin(new SimpleValueJqwikPlugin())
            .build();

    @Getter
    private final ArbitraryBuilder<Authority> authorityBuilder = fixtureMonkey.giveMeBuilder(Authority.class)
            .setNull(javaGetter(Authority::getId))
            .set(
                javaGetter(Authority::getAuthority),
                Arbitraries.strings().alpha().numeric().ofMinLength(12).ofMaxLength(255).withoutEdgeCases()
            );

    @Getter
    private final ArbitraryBuilder<Role> roleBuilder = fixtureMonkey.giveMeBuilder(Role.class)
            .setNull(javaGetter(Role::getId))
            .set(javaGetter(Role::getAuthorities), Collections.emptySet())
            .set(
                javaGetter(Role::getName),
                Arbitraries.strings().alpha().numeric().ofMinLength(12).ofMaxLength(255).withoutEdgeCases()
            );

    @Getter
    private final ArbitraryBuilder<User> userBuilder = fixtureMonkey.giveMeBuilder(User.class)
            .setNull(javaGetter(User::getAvatar))
            .setNull(javaGetter(User::getId))
            .set(
                javaGetter(User::getEmail),
                Arbitraries.strings().alpha().numeric().ofMinLength(12).ofMaxLength(255).withoutEdgeCases()
            )
            .set(
                javaGetter(User::getUsername),
                Arbitraries.strings().alpha().numeric().ofMinLength(12).ofMaxLength(255).withoutEdgeCases()
            );
}
