package org.zalando.logbook.json;

import com.google.common.io.Resources;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.zalando.logbook.json.JsonBodyFilters.accessToken;
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

class JsonPathBodyFiltersTest {

    private final String type = "application/json";
    private final String student;

    @SneakyThrows
    JsonPathBodyFiltersTest() {
        this.student = Resources.toString(getResource("student.json"), UTF_8);
    }

    private static final Configuration CONFIGURATION = Configuration.builder()
            .jsonProvider(new LogbookJacksonJsonProvider())
            .mappingProvider(new LogbookJacksonMappingProvider())
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    private DocumentContext parse(final String json) {
        return JsonPath.using(CONFIGURATION).parse(json);
    }

    @Test
    void deletesNumberAndString() {
        final BodyFilter unit = jsonPath("$.id").delete()
                .tryMerge(jsonPath("$.name").delete());

        final DocumentContext result = parse(requireNonNull(unit).filter(type, student));
        assertThat(result.read("$.id", Object.class)).isNull();
        assertThat(result.read("$.name", Object.class)).isNull();
    }

    @Test
    void deletesArray() {
        final BodyFilter unit = jsonPath("$.friends").delete();

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends", Object.class)).isNull();
    }

    @Test
    void deletesObject() {
        final BodyFilter unit = jsonPath("$.grades").delete();

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.grades", Object.class)).isNull();
    }

    @Test
    void replacesArrayWithString() {
        final BodyFilter unit = jsonPath("$.friends").replace("XXX");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends", String.class)).isEqualTo("XXX");
    }

    @Test
    void replacesNumberWithString() {
        final BodyFilter unit = jsonPath("$.id").replace("XXX");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.id", String.class)).isEqualTo("XXX");
    }

    @Test
    void replacesArrayWithNumber() {
        final BodyFilter unit = jsonPath("$.friends").replace(0.0);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends", Number.class)).isEqualTo(0.0);
    }

    @Test
    void replacesNumberWithNumbers() {
        final BodyFilter unit = jsonPath("$.grades.English").replace(1.0);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.grades.English", Number.class)).isEqualTo(1.0);
    }

    @Test
    void replacesArrayWithBoolean() {
        final BodyFilter unit = jsonPath("$.friends").replace(false);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends", Boolean.class)).isEqualTo(false);
    }

    @Test
    void replacesNumberWithBoolean() {
        final BodyFilter unit = jsonPath("$.id").replace(true);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.id", Boolean.class)).isEqualTo(true);
    }

    @Test
    void replacesStringDynamically() {
        final BodyFilter unit = jsonPath("$.name").replace(compile("^(\\w).+"), "$1.");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.name", String.class)).isEqualTo("A.");
    }

    @Test
    void replacesArrayDynamically() {
        final BodyFilter unit = jsonPath("$.friends.*.name").replace(compile("^(\\w).+"), "$1.");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends[0].name", String.class)).isEqualTo("B.");
        assertThat(result.read("$.friends[1].name", String.class)).isEqualTo("C.");
    }

    @Test
    void fallsBackTorReplaceArrayAsString() {
        final BodyFilter unit = jsonPath("$.friends").replace(compile("([A-Z])[a-z]+"), "$1.");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends", String.class))
                .isEqualToIgnoringWhitespace("[{\"id\":2,\"name\":\"B.\"},{\"id\":3,\"name\":\"C.\"}]");
    }

    @Test
    void replacesObjectDynamically() {
        final BodyFilter unit = jsonPath("$.grades.*").replace("XXX");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.grades.Math", String.class)).isEqualTo("XXX");
        assertThat(result.read("$.grades.English", String.class)).isEqualTo("XXX");
        assertThat(result.read("$.grades.Science", String.class)).isEqualTo("XXX");
        assertThat(result.read("$.grades.PE", String.class)).isEqualTo("XXX");
    }

    @Test
    void replacesValuesDynamically() {
        final BodyFilter unit = jsonPath("$.name").replace(String::toUpperCase);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.name", String.class)).isEqualTo("ALICE");
    }

    @Test
    void replacesValuesDynamicallyWithNullValue() {
        final BodyFilter unit = jsonPath("$.nickname").replace(String::toUpperCase);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.nickname", Object.class)).isNull();
    }

    @Test
    void replacesArrayValuesDynamically() {
        final BodyFilter unit = jsonPath("$.friends.*.name").replace(String::toUpperCase);

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.friends[0].name", String.class)).isEqualTo("BOB");
        assertThat(result.read("$.friends[1].name", String.class)).isEqualTo("CHARLIE");
    }

    @Test
    void fallsBackTorReplaceObjectAsString() {
        final BodyFilter unit = jsonPath("$.grades").replace(compile("(\\d+)\\.\\d+"), "$1.X");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.grades", String.class))
                .isEqualToIgnoringWhitespace("{\"Math\":1.X,\"English\":2.X,\"Science\":1.X,\"PE\":4.X}");
    }

    @Test
    void leavesNonMatchingNumberInPlace() {
        final BodyFilter unit = jsonPath("$.id").replace(compile("\\s+"), "XXX");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.id", Integer.class)).isEqualTo(1);
    }

    @Test
    void leavesNonMatchingStringInPlace() {
        final BodyFilter unit = jsonPath("$.name").replace(compile("\\s+"), "XXX");

        final DocumentContext result = parse(unit.filter(type, student));
        assertThat(result.read("$.name", String.class)).isEqualTo("Alice");
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

    @Test
    void shouldNotFailWhenOneOfTheFiltersInCompositeThrowsException() {
        final BodyFilter nameFilter = jsonPath("$.name").replace("XXX");
        final BodyFilter nonExistingFieldFilter = jsonPath("$[0].thisFieldDoesntExist").delete();
        final BodyFilter anotherNonExistingFieldFilter = jsonPath("$.nonExistingArray[0].thisFieldDoesntExist").delete();
        final BodyFilter addressFilter = jsonPath("$.address").replace( "XXX");
        final BodyFilter unit = nameFilter
                .tryMerge(nonExistingFieldFilter)
                .tryMerge(anotherNonExistingFieldFilter)
                .tryMerge(addressFilter);

        final DocumentContext result = parse(requireNonNull(unit).filter(type, student));
        assertThat(result.read("$.name", String.class)).isEqualTo("XXX");
        assertThat(result.read("$.address", String.class)).isEqualTo("XXX");
    }
}
