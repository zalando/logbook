package org.zalando.logbook.netty;

import com.google.gag.annotation.remark.ThisWouldBeOneLineIn;
import org.zalando.logbook.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Collections.singletonList;

interface HeaderSupport {

    default HttpHeaders copyOf(final Iterable<Entry<String, String>> entries) {
        HttpHeaders headers = HttpHeaders.empty();

        for (final Entry<String, String> entry : entries) {
            headers = append(headers, entry);
        }

        return headers;
    }

    // Effectively package-private because this interface is and so are all
    // implementations of it. Ideally it would be private
    @ThisWouldBeOneLineIn(language = "Java 9", toWit = "private")
    default HttpHeaders append(
            final HttpHeaders headers, final Entry<String, String> entry) {

        return headers.apply(entry.getKey(), previous -> {
            if (previous == null) {
                return singletonList(entry.getValue());
            }

            final List<String> result = new ArrayList<>(previous);
            result.add(entry.getValue());
            return result;
        });
    }

}
