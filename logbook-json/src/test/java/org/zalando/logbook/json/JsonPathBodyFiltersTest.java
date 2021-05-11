package org.zalando.logbook.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

class JsonPathBodyFiltersTest {

    public static final String CONTENT_TYPE = "application/json";

    @Test
    public void deleteObjectTest() {
        BodyFilter filter = jsonPath("$.test.test123").delete();
        String result = filter.filter(CONTENT_TYPE, "{\"test\": {\"test123\":\"testvalue\"}, \"test2\":\"value\"}");
        assertThat(result).isEqualTo("{\"test\":{},\"test2\":\"value\"}");
    }

    @Test
    public void replaceArrayWithStringTest() {
        BodyFilter filter = jsonPath("$.test").replace("XXX");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": [\"test123\", \"test321\", 1.0], \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":\"XXX\",\"test123\":\"testing\"}");
    }

    @Test
    public void replaceNumberWithStringTest() {
        BodyFilter filter = jsonPath("$.test").replace("XXX");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": 1, \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":\"XXX\",\"test123\":\"testing\"}");
    }

    @Test
    public void replaceArrayWithNumberTest() {
        BodyFilter filter = jsonPath("$.test").replace(10.0);
        String result = filter.filter(CONTENT_TYPE, "{\"test\": [\"test123\", \"test321\", 1.0], \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":10.0,\"test123\":\"testing\"}");
    }

    @Test
    public void replaceNumberWithNumberTest() {
        BodyFilter filter = jsonPath("$.test").replace(10.0);
        String result = filter.filter(CONTENT_TYPE, "{\"test\": 1, \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":10.0,\"test123\":\"testing\"}");
    }

    @Test
    public void replaceArrayWithBooleanTest() {
        BodyFilter filter = jsonPath("$.test").replace(true);
        String result = filter.filter(CONTENT_TYPE, "{\"test\": [\"test123\", \"test321\", 1.0], \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":true,\"test123\":\"testing\"}");
    }

    @Test
    public void replaceNumberWithBooleanTest() {
        BodyFilter filter = jsonPath("$.test").replace(true);
        String result = filter.filter(CONTENT_TYPE, "{\"test\": 1, \"test123\": \"testing\"}");
        assertThat(result).isEqualTo("{\"test\":true,\"test123\":\"testing\"}");
    }

    @Test
    public void replaceStringDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test").replace("(\\d{6})\\d+(\\d{4})", "$1******$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": \"5213486633218931\", \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":\"521348******8931\",\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void replaceArrayDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test").replace("(\\d{6})\\d+(\\d{4})", "$1******$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": [\"5213486633218931\", \"123\", {}, true], \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":\"[\\\"521348******8931\\\",\\\"123\\\",{},true]\",\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void replaceArrayInRightWayDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test.*").replace("(\\d{6})\\d+(\\d{4})", "$1******$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": [\"5213486633218931\", \"123\", {}, true], \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":[\"521348******8931\",\"123\",{},true],\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void replaceObjectDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test").replace("(\\d{6})\\d+(\\d{4})", "$1******$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": {\"321test\": \"5213486633218931\"}, \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":\"{321test=521348******8931}\",\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void unsuccessfullReplaceStringDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test").replace("\\s+", "$1********$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\":5213, \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":5213,\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void unsuccessfullReplaceNumberDynamicallyTest() {
        BodyFilter filter = jsonPath("$.test").replace("\\s+", "$1********$2");
        String result = filter.filter(CONTENT_TYPE, "{\"test\": \"5213486633218931\", \"test123\": \"5213486633218931\"}");
        assertThat(result).isEqualTo("{\"test\":\"5213486633218931\",\"test123\":\"5213486633218931\"}");
    }

    @Test
    public void contentTypeTest() {
        BodyFilter filter = jsonPath("$.test").replace("XXX");
        String result = filter.filter("application/xml", "{\"test\": \"value\"}");
        assertThat(result).isEqualTo("{\"test\": \"value\"}");
    }
}