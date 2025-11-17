package api.services;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

import java.util.Collections;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import api.entities.Authority;
import api.entities.Role;
import api.entities.User;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.Getter;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * {@link FixtureService}.
 */
@Service
public class FixtureService {
    private static final Arbitrary<String> DEFAULT_STRING_ARBITRARY =
        Arbitraries.strings().alpha().numeric().ofMinLength(12).ofMaxLength(255).withoutEdgeCases();

    private final Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> authorityBuilderFunc =
        fm -> fm.giveMeBuilder(Authority.class).setNull(javaGetter(Authority::getId))
            .set(javaGetter(Authority::getAuthority), DEFAULT_STRING_ARBITRARY);

    private final Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> roleBuilderFunc =
        fm -> fm.giveMeBuilder(Role.class).setNull(javaGetter(Role::getId))
            .set(javaGetter(Role::getAuthorities), Collections.emptySet())
            .set(javaGetter(Role::getName), DEFAULT_STRING_ARBITRARY);

    private final Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> userBuilderFunc =
        fm -> fm.giveMeBuilder(User.class).setNull(javaGetter(User::getAvatar)).setNull(javaGetter(User::getId))
            .set(javaGetter(User::getEmail), DEFAULT_STRING_ARBITRARY)
            .set(javaGetter(User::getUsername), DEFAULT_STRING_ARBITRARY);

    @Getter
    private final FixtureMonkey fixtureMonkey =
        FixtureMonkey.builder().plugin(new JakartaValidationPlugin()).plugin(new SimpleValueJqwikPlugin())
            .defaultNullInjectGenerator(context -> 0.0).register(Authority.class, authorityBuilderFunc)
            .register(Role.class, roleBuilderFunc).register(User.class, userBuilderFunc).build();
}
