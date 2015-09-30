package org.zalando.logbook;

import com.google.common.collect.Multimap;

public interface HttpResponse extends HttpMessage {

    int getStatus();

}
