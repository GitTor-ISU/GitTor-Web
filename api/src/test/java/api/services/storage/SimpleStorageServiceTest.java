package api.services.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import api.BasicContext;

/**
 * {@link SimpleStorageService} test.
 */
public class SimpleStorageServiceTest extends BasicContext {
    @Autowired(required = false)
    private MinioStorageService minioStorageService;

    private MemoryStorageService memoryStorageService;
    private LocalStorageService localStorageService;
    private List<SimpleStorageService> storages;

    static Stream<Object[]> allPermutations() {
        List<Object[]> permutations = new ArrayList<>();

        String[] keys = {"key", "", " ", " key", "key ", "/root", "../relative", null};
        byte[][] streams = {"test".getBytes(), new byte[0], null};
        long[] sizes = {-1, 0, 1, 100};

        for (String key : keys) {
            for (byte[] data : streams) {
                // Correct size
                if (data != null) {
                    permutations.add(new Object[]{key, data, data.length});
                }

                // Other sizes
                for (long size : sizes) {
                    permutations.add(new Object[]{key, data, size});
                }
            }
        }

        return permutations.stream();
    }

    @BeforeEach
    public void setup() {
        memoryStorageService = new MemoryStorageService();
        localStorageService = new LocalStorageService();

        localStorageService.setRootPath("./temp/s3/");
        localStorageService.init();

        if (minioStorageService != null) {
            storages = List.of(memoryStorageService, localStorageService, minioStorageService);
        } else {
            storages = List.of(memoryStorageService, localStorageService);
        }
    }

    @Test
    public void shouldMatchCaseSensitive() throws IOException {
        String key = "key";
        String lowerKey = key.toLowerCase();
        String upperKey = key.toUpperCase();

        byte[] lowerData = "lower".getBytes();
        long lowerSize = lowerData.length;

        byte[] upperData = "upper".getBytes();
        long upperSize = upperData.length;

        String[][] downloads = new String[storages.size()][2];
        for (String[] row : downloads) {
            Arrays.fill(row, null);
        }
        for (int i = 0; i < storages.size(); i++) {
            SimpleStorageService s3 = storages.get(i);

            // Upload lower object
            s3.uploadObject(lowerKey, new ByteArrayInputStream(lowerData), lowerSize);

            // Upload upper object
            s3.uploadObject(upperKey, new ByteArrayInputStream(upperData), upperSize);

            // Download lower object
            downloads[i][0] = new String(s3.downloadObject(lowerKey).readAllBytes());

            // Download upper object
            downloads[i][1] = new String(s3.downloadObject(upperKey).readAllBytes());
        }

        assertAll(
            // Downloads match
            IntStream.range(0, downloads[0].length)
                .boxed()
                .flatMap(j ->
                    IntStream.range(1, storages.size())
                        .mapToObj(i -> (Executable) () -> {
                            if (downloads[0][j] != null) {
                                assertEquals(downloads[0][j], downloads[i][j],
                                    "Download " + j + " mismatch between storage 0 and " + i);
                            } else {
                                assertNull(downloads[i][j],
                                    "Download " + j + " failed for storage 0, but succeeded for storage " + i);
                            }
                        })
                )
        );
    }

    @ParameterizedTest
    @MethodSource("allPermutations")
    public void shouldMatchEdgeCases(String key, byte[] data, long size) {
        byte[] overwriteData = "overwriteData".getBytes();
        long overwriteSize = overwriteData.length;

        String[][] downloads = new String[storages.size()][3];
        Exception[][] exceptions = new Exception[storages.size()][6];

        for (String[] row : downloads) {
            Arrays.fill(row, null);
        }
        for (Exception[] row : exceptions) {
            Arrays.fill(row, null);
        }

        for (int i = 0; i < storages.size(); i++) {
            SimpleStorageService s3 = storages.get(i);

            // Upload object
            try {
                s3.uploadObject(key, new ByteArrayInputStream(data), size);
            } catch (Exception e) {
                exceptions[i][0] = e;
            }

            // Download object
            try {
                downloads[i][0] = new String(s3.downloadObject(key).readAllBytes());
            } catch (Exception e) {
                exceptions[i][1] = e;
            }

            // Overwrite object
            try {
                s3.uploadObject(key, new ByteArrayInputStream(overwriteData), overwriteSize);
            } catch (Exception e) {
                exceptions[i][2] = e;
            }

            // Download object
            try {
                downloads[i][1] = new String(s3.downloadObject(key).readAllBytes());
            } catch (Exception e) {
                exceptions[i][3] = e;
            }

            // Delete object
            try {
                s3.deleteObject(key);
            } catch (Exception e) {
                exceptions[i][4] = e;
            }

            // Download object
            try {
                downloads[i][2] = new String(s3.downloadObject(key).readAllBytes());
            } catch (Exception e) {
                exceptions[i][5] = e;
            }
        }

        List<Executable> all = Stream.concat(
            // Downloads match
            IntStream.range(0, downloads[0].length)
                .boxed()
                .flatMap(j ->
                    IntStream.range(1, storages.size())
                        .mapToObj(i -> (Executable) () -> {
                            if (downloads[0][j] != null) {
                                assertEquals(downloads[0][j], downloads[i][j],
                                    "Download " + j + " mismatch between storage 0 and " + i);
                            } else {
                                assertNull(downloads[i][j],
                                    "Download " + j + " failed for storage 0, but succeeded for storage " + i);
                            }
                        })
                ),

            // Exceptions match
            IntStream.range(0, exceptions[0].length)
                .boxed()
                .flatMap(j ->
                    IntStream.range(1, storages.size())
                        .mapToObj(i -> (Executable) () -> {
                            if (exceptions[0][j] != null) {
                                assertEquals(exceptions[0][j].getClass(), exceptions[i][j].getClass(),
                                    "Step " + j + " exception mismatch between storage 0 and " + i);
                            } else {
                                assertNull(exceptions[i][j],
                                    "Step " + j + " no exception for storage 0, but exception for storage " + i);
                            }
                        })
                )
        ).toList();

        assertAll(all);
    }
}
