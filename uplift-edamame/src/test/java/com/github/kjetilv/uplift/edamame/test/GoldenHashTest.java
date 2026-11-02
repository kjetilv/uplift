package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.impl.RecursiveTreeHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Fixed inputs with recorded hashes. A failure here means the hash format has changed.
///
/// Update the recorded values only if the change is intended. Hashes are not known to be stored anywhere,
/// so a format change has no effect outside a running process.
class GoldenHashTest {

    @TestFactory
    Stream<DynamicTest> goldenHashes() {
        return Stream.concat(
            GOLDEN.stream().map(golden -> test(HashKind.K128, golden.name(), golden.value(), golden.k128())),
            GOLDEN.stream().map(golden -> test(HashKind.K256, golden.name(), golden.value(), golden.k256()))
        );
    }

    private record Golden(String name, Object value, String k128, String k256) {
    }

    private static final List<Golden> GOLDEN = List.of(
        new Golden(
            "string", "foo",
            "LCwoSlFyo2WkkmTqmXAbpw",
            "AJzdTjCcAa0ZTP1x7NYQSw8Q-aZ_YnUZL1qTJS3YOsA"
        ),
        new Golden(
            "long", 42L,
            "FJmmG9amoP5r-vRzU8lEug",
            "6g9ygxkt5hNLbk6lWXHCbI9-u9X9uWalWKijXcsCqOU"
        ),
        new Golden(
            "int", 42,
            "JoiEHLdm9kCFVdLwcr3O-g",
            "GKgyzxIFC0nxT5NP6nJutTgERkP1lmWXB2lOYRCZXPY"
        ),
        new Golden(
            "double", 1.5d,
            "SsRZeGtu7PY_tMlAETNy1g",
            "VaZfZ1M0eOSBSUT0gZMNemoyOhgWP5HGkcUySCyPD6k"
        ),
        new Golden(
            "bool", true,
            "3lCA0wbCPEeCAaH0EndE0g",
            "wy4AHV617MLWdcP7HIo0s-QQcsspymtwUbC3KwJwHwE"
        ),
        new Golden(
            "bigDecimal", new BigDecimal("1.50"),
            "qwJdIf-Z-Sj0fLgHTF4_hA",
            "FdvFGXKYyOImarXgRXuZqxOl1LmhQQQQZ1aQ_SXodUo"
        ),
        new Golden(
            "uuid", UUID.fromString("6f1c2d3e-4a5b-4c6d-8e7f-001122334455"),
            "sPXCMO3UXZdcxQ73m14bVw",
            "5uNeyLYi7wwlej5coiMq99h_rpqNdWdDJX4QNN9_EqA"
        ),
        new Golden(
            "instant", Instant.parse("2024-01-01T12:00:00.000000001Z"),
            "_Vs8LOMi-223_FK7E2qJRg",
            "NmYCPa_TzGweV9nHGWJ_sULMVnED37TuavnYFuJDWi4"
        ),
        new Golden(
            "localDate", LocalDate.of(2024, 2, 29),
            "Bsr5cktxUIjCaF9Wm05i0w",
            "NsW2IX1ZICdvtfaN_eLPpJKi1MjG2nbzjnGTeKQIrzI"
        ),
        new Golden(
            "emptyList", List.of(),
            "KYCBeIxRN2bBLY1vCKJC_A",
            "urQzJF2Eu6lPhr0_i8K9xofR1o8rrKcK_OTG9GROA6c"
        ),
        new Golden(
            "emptyMap", Map.of(),
            "SrjvJhdaStUl3aRWHCP4FQ",
            "Rj4eev-AopOCHj8ZcLCvAiaY2p1Vt6zIAI0_WEl7PWY"
        ),
        new Golden(
            "list", List.of(1L, "a"),
            "EJ9jaXTKcUrSinBlKYjFcQ",
            "u0Sj1sJWv5xVjS9QdTICruRKfyaiwvBgTbsFr5maNd0"
        ),
        new Golden(
            "map", Map.of("a", 1L, "b", List.of("x")),
            "84Qmj9VojFNflZuX-2zCVw",
            "FUCekSgW-G3lyMGGRqcRRnQTApmjEOe5zWkoILbVP5c"
        )
    );

    private static <H extends HashKind<H>> DynamicTest test(H kind, String name, Object value, String digest) {
        return dynamicTest(kind.getClass().getSimpleName() + " " + name, () -> {
            var hasher = new RecursiveTreeHasher<String, H>(
                () -> HashBuilder.forKind(kind),
                KeyHandler.defaultHandler(),
                LeafHasher.create(kind),
                kind
            );
            assertThat(hasher.tree(value).hash().digest()).isEqualTo(digest);
        });
    }
}
