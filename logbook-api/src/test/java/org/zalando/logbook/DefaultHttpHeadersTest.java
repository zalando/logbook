package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.DefaultHttpHeaders.EMPTY;

class DefaultHttpHeadersTest {

    @Test
    void checkEmptyHeadersSize() {
        assertThat(EMPTY).hasSize(0);
    }

    @Test
    void checkUpdateHeadersWorksAndIsCaseInsensitive() {
        List<String> lst1 = Arrays.asList("v1", "v2");
        List<String> lst2 = Arrays.asList("v3", "v4", "v5");
        List<String> lst3 = Arrays.asList("v4", "v5");
        HttpHeaders headers = EMPTY
                .update("HEADER1", lst1)
                .update("header0", lst2)
                .update("header1", lst3);

        TreeMap<String, List<String>> expectedMap = new TreeMap<>();
        expectedMap.put("header0", lst2);
        expectedMap.put("HEADER1", lst3);

        assertThat(headers).isEqualTo(expectedMap);
    }

    @Test
    void checkUpdateHeadersIsImmutable() {
        List<String> lst1 = new ArrayList<>();
        lst1.add("v1");
        lst1.add("v2");

        HttpHeaders headers = EMPTY.update("H1", lst1);

        lst1.add("v3");

        TreeMap<String, List<String>> expectedMap = new TreeMap<>();
        expectedMap.put("H1", Arrays.asList("v1", "v2"));

        assertThat(headers).isEqualTo(expectedMap);
    }

    @Test
    void checkDeleteHeadersWorksAndIsCaseInsensitive() {
        List<String> lst1 = Arrays.asList("v1", "v2");
        List<String> lst2 = Arrays.asList("v3", "v4", "v5");
        List<String> lst3 = Arrays.asList("v4", "v5");
        HttpHeaders headers = EMPTY
                .update("UPPERCASE", lst1)
                .update("lowercase", lst2)
                .update("MixedCase", lst3)
                .delete(Arrays.asList("uppercase", "LOWERCASE"));

        TreeMap<String, List<String>> expectedMap = new TreeMap<>();
        expectedMap.put("MixedCase", lst3);

        assertThat(headers).isEqualTo(expectedMap);
    }

}
