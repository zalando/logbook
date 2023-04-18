package org.zalando.logbook.json;

import com.google.common.io.Resources;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.BodyFilter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.zalando.logbook.json.JsonBodyFilters.accessToken;
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

@SuppressWarnings("UnstableApiUsage")
class JsonPathBodyFiltersTest {

    private final String type = "application/json";
    private final String student;

    @SneakyThrows
    JsonPathBodyFiltersTest() {
        this.student = Resources.toString(getResource("student.json"), UTF_8);
    }

    @BeforeAll
    static void beforeAll() {
        Configuration.setDefaults(new Defaults() {

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new JacksonMappingProvider();
            }

            @Override
            public JsonProvider jsonProvider() {
                return new JacksonJsonProvider();
            }
        });
    }

    @Test
    void deletesNumberAndString() {
        final BodyFilter unit = jsonPath("$.id").delete()
                .tryMerge(jsonPath("$.name").delete());

        with(requireNonNull(unit).filter(type, student))
                .assertNotDefined("id")
                .assertNotDefined("name");
    }

    @Test
    void deletesArray() {
        final BodyFilter unit = jsonPath("$.friends").delete();

        with(unit.filter(type, student))
                .assertNotDefined("friends");
    }

    @Test
    void deletesObject() {
        final BodyFilter unit = jsonPath("$.grades").delete();

        with(unit.filter(type, student))
                .assertNotDefined("grades");
    }

    @Test
    void replacesArrayWithString() {
        final BodyFilter unit = jsonPath("$.friends").replace("XXX");

        with(unit.filter(type, student))
                .assertEquals("friends", "XXX");
    }

    @Test
    void replacesNumberWithString() {
        final BodyFilter unit = jsonPath("$.id").replace("XXX");

        with(unit.filter(type, student))
                .assertEquals("id", "XXX");
    }

    @Test
    void replacesArrayWithNumber() {
        final BodyFilter unit = jsonPath("$.friends").replace(0.0);

        with(unit.filter(type, student))
                .assertEquals("friends", 0.0);
    }

    @Test
    void replacesNumberWithNumbers() {
        final BodyFilter unit = jsonPath("$.grades.English").replace(1.0);

        with(unit.filter(type, student))
                .assertEquals("grades.English", 1.0);
    }

    @Test
    void replacesArrayWithBoolean() {
        final BodyFilter unit = jsonPath("$.friends").replace(false);

        with(unit.filter(type, student))
                .assertEquals("friends", false);
    }

    @Test
    void replacesNumberWithBoolean() {
        final BodyFilter unit = jsonPath("$.id").replace(true);

        with(unit.filter(type, student))
                .assertEquals("id", true);
    }

    @Test
    void replacesStringDynamically() {
        final BodyFilter unit = jsonPath("$.name").replace(compile("^(\\w).+"), "$1.");

        with(unit.filter(type, student))
                .assertEquals("name", "A.");
    }

    @Test
    void replacesArrayDynamically() {
        final BodyFilter unit = jsonPath("$.friends.*.name").replace(compile("^(\\w).+"), "$1.");

        with(unit.filter(type, student))
                .assertEquals("friends[0].name", "B.")
                .assertEquals("friends[1].name", "C.");
    }

    @Test
    void fallsBackTorReplaceArrayAsString() {
        final BodyFilter unit = jsonPath("$.friends").replace(compile("([A-Z])[a-z]+"), "$1.");

        with(unit.filter(type, student))
                .assertEquals("friends", "[{\"id\":2,\"name\":\"B.\"},{\"id\":3,\"name\":\"C.\"}]");
    }

    @Test
    void replacesObjectDynamically() {
        final BodyFilter unit = jsonPath("$.grades.*").replace("XXX");

        with(unit.filter(type, student))
                .assertEquals("grades.Math", "XXX")
                .assertEquals("grades.English", "XXX")
                .assertEquals("grades.Science", "XXX")
                .assertEquals("grades.PE", "XXX");
    }

    @Test
    void replacesValuesDynamically() {
        final BodyFilter unit = jsonPath("$.name").replace(String::toUpperCase);

        with(unit.filter(type, student))
                .assertEquals("name", "ALICE");
    }

    @Test
    void replacesValuesDynamicallyWithNullValue() {
        final BodyFilter unit = jsonPath("$.nickname").replace(String::toUpperCase);

        with(unit.filter(type, student))
                .assertEquals("nickname", null);
    }

    @Test
    void replacesArrayValuesDynamically() {
        final BodyFilter unit = jsonPath("$.friends.*.name").replace(String::toUpperCase);

        with(unit.filter(type, student))
                .assertEquals("friends[0].name", "BOB")
                .assertEquals("friends[1].name", "CHARLIE");
    }

    @Test
    void fallsBackTorReplaceObjectAsString() {
        final BodyFilter unit = jsonPath("$.grades").replace(compile("(\\d+)\\.\\d+"), "$1.X");

        with(unit.filter(type, student))
                .assertEquals("grades", "{\"Math\":1.X,\"English\":2.X,\"Science\":1.X,\"PE\":4.X}");
    }

    @Test
    void leavesNonMatchingNumberInPlace() {
        final BodyFilter unit = jsonPath("$.id").replace(compile("\\s+"), "XXX");

        with(unit.filter(type, student))
                .assertEquals("id", 1);
    }

    @Test
    void leavesNonMatchingStringInPlace() {
        final BodyFilter unit = jsonPath("$.name").replace(compile("\\s+"), "XXX");

        with(unit.filter(type, student))
                .assertEquals("name", "Alice");
    }

    @Test
    void filtersJsonOnly() {
        final BodyFilter unit = jsonPath("$.test").replace("XXX");

        assertThat(unit.filter("application/xml", student))
                .isEqualTo(student);
    }

    @Test
    void mergesOnlyWithJsonPathBodyFilter() {
        final BodyFilter unit = jsonPath("$.test").replace("XXX");

        assertNull(unit.tryMerge(accessToken()));
    }

    @Test
    void doesNotFailOnMissingPath() {
        final BodyFilter unit = jsonPath("$.friends.missing").delete();

        assertThat(unit.filter("application/json", student))
            .isEqualToIgnoringWhitespace(student);
    }

    @Test
    void doesNotFailWhenBodyIsUnwrappedArray() throws IOException {
        String cars = Resources.toString(getResource("cars-unwrapped-array.json"), UTF_8);

        final BodyFilter unit = jsonPath("$.name").replace(String::toUpperCase);

        unit.filter(type, cars);
    }

    @Test
    void doesNotFailWhenBodyIsEmpty() {
        final BodyFilter unit = jsonPath("$.id").replace(compile("\\s+"), "XXX");

        String actual = unit.filter(type, "");

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnSameBodyWhenBodyIsInvalidJson() {
        String invalidBody = "{\"id\": 1, \"name\": \"Alice\",}";
        final BodyFilter unit = jsonPath("$.id").replace(compile("\\s+"), "XXX");

        String actual = unit.filter(type, invalidBody);

        assertThat(actual).isEqualTo(invalidBody);
    }

    @Test
    void shouldNotFailWhenThereAreNullNodes() {
        String invalidBody = "{\"name\":null}";
        final BodyFilter unit = jsonPath("$.name").replace(compile("\\s+"), "XXX");

        String actual = unit.filter(type, invalidBody);

        assertThat(actual).isEqualTo(invalidBody);
    }
}
