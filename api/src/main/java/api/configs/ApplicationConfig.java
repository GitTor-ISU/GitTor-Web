package api.configs;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

/**
 * {@link ApplicationConfig}.
 */
@Configuration
public class ApplicationConfig {
    /**
     * clock.
     *
     * @return {@link Clock}.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * minioClient.
     *
     * @param url URL to minio server
     * @param username Username
     * @param password Password
     * @return {@link MinioClient}
     */
    @Bean
    @Profile("prod")
    public MinioClient minioClient(
        @Value("${s3.url}") String url,
        @Value("${s3.username:s3_user}") String username,
        @Value("${s3.password:s3_password}") String password
    ) {
        return MinioClient.builder()
            .endpoint(url)
            .credentials(username, password)
            .build();
    }
}
