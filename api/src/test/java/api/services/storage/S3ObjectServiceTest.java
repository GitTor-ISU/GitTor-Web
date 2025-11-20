package api.services.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import api.BasicContext;
import api.entities.S3Object;
import api.services.S3ObjectService;

/**
 * {@link S3ObjectService} test.
 */
public class S3ObjectServiceTest extends BasicContext {
    @Autowired
    private S3ObjectService s3ObjectService;

    @Value("${api.s3.max}")
    private long maxObjectSize;

    @Test
    public void saveShouldFail_whenSizeTooLarge() {
        int size = (int) (maxObjectSize + 1);
        byte[] content = new byte[size];

        S3Object object = S3Object.builder().size(size).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> s3ObjectService.save(object, new ByteArrayInputStream(content)));

        assertEquals("File size exceeds limit (" + maxObjectSize + " bytes).", ex.getMessage());
    }
}
