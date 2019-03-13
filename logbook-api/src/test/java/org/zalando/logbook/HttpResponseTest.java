package org.zalando.logbook;


import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpResponseTest {

    // https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
    private static List<Integer> RESPONSE_CODES = Arrays.asList(new Integer[] {
            // official
            100, 101, 102, 
            200, 201, 202, 203, 204, 205, 206, 207, 208, 226,
            300, 301, 302, 303, 304, 305, 306, 307, 308,
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 421, 422, 423, 424, 426, 428, 429, 431, 451,
            500, 501, 502, 503, 504, 505, 506, 507, 508, 510, 511,
            // unofficial
            // nginx
            444, 494, 495, 496, 497, 499,
            // unknown
            599
            
    });

    @Test
    public void testKnownCodes() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getReasonPhrase()).thenCallRealMethod();
        
        // check non-null responses
        for(int i = 0; i < RESPONSE_CODES.size(); i++) {
            when(response.getStatus()).thenReturn(RESPONSE_CODES.get(i));
            assertNotNull(response.getReasonPhrase(), "No reason phrase for status code " + RESPONSE_CODES.get(i));
        }
        
        // test message contents for a few responses
        when(response.getStatus()).thenReturn(200);
        assertThat(response.getReasonPhrase(), is("OK"));
        
        when(response.getStatus()).thenReturn(400);
        assertThat(response.getReasonPhrase(), is("Bad Request"));
        
    }
    
    @Test
    public void testEmptyCodes() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getReasonPhrase()).thenCallRealMethod();
        
        Set<Integer> list = new HashSet<>(RESPONSE_CODES);
        for(int i = 0; i < 1000; i++) {
            when(response.getStatus()).thenReturn(i);
            if(response.getReasonPhrase() != null && !list.remove(Integer.valueOf(i))) {
                fail("Unexpected reason phrase for code " + i);
            }
        }
        assertThat(list, IsEmptyCollection.empty());
    }
    
}
