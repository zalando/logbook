package org.zalando.logbook;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.UnmodifiableIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.collect.Iterators.addAll;
import static com.google.common.collect.Iterators.forEnumeration;

// TODO sort headers by name?!
final class Formatting {

    static Multimap<String, String> getHeaders(final HttpServletRequest request) {
        final Multimap<String, String> headers = ArrayListMultimap.create();
        final UnmodifiableIterator<String> iterator = forEnumeration(request.getHeaderNames());

        while (iterator.hasNext()) {
            final String header = iterator.next();
            addAll(headers.get(header), forEnumeration(request.getHeaders(header)));
        }

        return headers;
    }


    static Multimap<String, String> getHeaders(final HttpServletResponse response) {
        final Multimap<String, String> headers = ArrayListMultimap.create();

        for (final String header : response.getHeaderNames()) {
            headers.putAll(header, response.getHeaders(header));
        }

        return headers;
    }

}
