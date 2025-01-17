package org.zalando.logbook.json;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonJsonFieldBodyFilterTest {

    @Test
    public void testFilterString() throws Exception {
        final String filtered = getFilter("email").filter(getResource("/user.json"));
        assertThat(filtered).doesNotContain("@entur.org");
    }

    @Test
    public void testFilterNumber() throws Exception {
        final String filtered = getFilter("id").filter(getResource("/user.json"));
        assertThat(filtered).doesNotContain("18375");
    }

    @Test
    public void testFilterObject() throws Exception {
        final String filtered = getFilter("cars").filter(getResource("/cars-object.json"));
        assertThat(filtered).doesNotContain("Ford");
    }

    @Test
    public void testFilterArray() throws Exception {
        final String filtered = getFilter("cars").filter(getResource("/cars-array.json"));
        assertThat(filtered).doesNotContain("Ford");
    }

    @Test
    public void testFilterHugeBodyObject1() throws Exception {
        final String string = getResource("/huge-sample.json");
        final String filtered = getFilter("name").filter(string);
        assertThat(filtered)
                .doesNotContain("Pena Hudson")
                .hasSizeLessThan(string.length());
    }

    @Test
    public void testFilterHugeBodyObject2() throws Exception {
        final Set<String> remove = new HashSet<>();
        remove.add("name");
        final String string = getResource("/huge-sample.json");
        final String filtered = getFilter(remove).filter("application/json", string);
        assertThat(filtered)
                .doesNotContain("Pena Hudson")
                .hasSizeLessThan(string.length());
    }

    @Test
    public void doesNotFilterInvalidJson() throws Exception {
        final String valid = getResource("/cars-array.json").trim();
        final String invalid = valid.substring(0, valid.length() - 1);
        final String filtered = getFilter("cars").filter(invalid);
        assertThat(filtered).contains("Ford");
    }

    @Test
    public void doesNotFilterNonJson() throws Exception {
        final String valid = getResource("/cars-array.json").trim();
        final String invalid = valid.substring(0, valid.length() - 1);
        final String filtered = getFilter("cars").filter("application/xml", invalid);
        assertThat(filtered).contains("Ford");
    }

    @Test
    public void shouldPreserveBigFloatOnCopy() throws Exception {
        final String string = getResource("/student.json").trim();
        final JacksonJsonFieldBodyFilter filter = new JacksonJsonFieldBodyFilter(Collections.emptyList(), "XXX", new JsonFactory(), new PreciseFloatJsonGeneratorWrapperCreator());
        final String filtered = filter.filter("application/json", string);
        assertThat(filtered).contains("\"debt\":123450.40000000000000002");
    }

    private String getResource(final String path) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/" + path));
        return new String(bytes, UTF_8);
    }

    public static JacksonJsonFieldBodyFilter getFilter(final String... fieldNames) {
        return new JacksonJsonFieldBodyFilter(Arrays.asList(fieldNames), "XXX");
    }

    public static JacksonJsonFieldBodyFilter getFilter(final Collection<String> fieldNames) {
        return new JacksonJsonFieldBodyFilter(fieldNames, "XXX");
    }

}
