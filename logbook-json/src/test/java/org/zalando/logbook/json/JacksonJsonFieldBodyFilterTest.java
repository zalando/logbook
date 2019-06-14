package org.zalando.logbook.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;


public class JacksonJsonFieldBodyFilterTest {

    @Test
    public void testFilterString() throws Exception {
        String filtered = getFilter("email") .filter(getResource("/user.json"));
        assertThat(filtered, not(containsString("@entur.org")));
    }

    @Test
    public void testFilterNumber() throws Exception {
        String filtered = getFilter("id") .filter(getResource("/user.json"));
        assertThat(filtered, not(containsString("18375")));
    }

    @Test
    public void testFilterObject() throws Exception {
        String filtered = getFilter("cars") .filter(getResource("/cars-object.json"));
        assertThat(filtered, not(containsString("Ford")));
    }

    @Test
    public void testFilterArray() throws Exception {
        String filtered = getFilter("cars") .filter(getResource("/cars-array.json"));
        assertThat(filtered, not(containsString("Ford")));
    }

    @Test
    public void testFilterHugeBodyObject1() throws Exception {
        String string = getResource("/huge-sample.json");
        String filtered = getFilter("name").filter(string);
        assertThat(filtered, not(containsString("Pena Hudson")));
        assertThat(filtered.length(), is(lessThan(string.length())));
    }

    @Test
    public void testFilterHugeBodyObject2() throws Exception {
        Set<String> remove = new HashSet<>();
        remove.add("name");
        String string = getResource("/huge-sample.json");
        String filtered = getFilter(remove).filter("application/json", string);
        assertThat(filtered, not(containsString("Pena Hudson")));
        assertThat(filtered.length(), is(lessThan(string.length())));
    }
    
    @Test
    public void doesNotFilterInvalidJson() throws Exception {
        String valid = getResource("/cars-array.json").trim();
        String invalid = valid.substring(0, valid.length() - 1);
        String filtered = getFilter("cars").filter(invalid);
        assertThat(filtered, containsString("Ford"));
    }
    
    @Test
    public void doesNotFilterNonJson() throws Exception {
        String valid = getResource("/cars-array.json").trim();
        String invalid = valid.substring(0, valid.length() - 1);
        String filtered = getFilter("cars").filter("application/xml", invalid);
        assertThat(filtered, containsString("Ford"));
    }    

    private String getResource(String path) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/" + path));
        return new String(bytes, UTF_8);
    }

    public static JacksonJsonFieldBodyFilter getFilter(String ... fieldNames) {
        return new JacksonJsonFieldBodyFilter(Arrays.asList(fieldNames), "XXX");
    }

    public static JacksonJsonFieldBodyFilter getFilter(Collection<String> fieldNames) {
        return new JacksonJsonFieldBodyFilter(fieldNames , "XXX");
    }

}
