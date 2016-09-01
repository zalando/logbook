package org.zalando.logbook.httpclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

interface Localhost {

    default String getAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    static Localhost resolve() {
        return new Localhost() {
            // rely on defaults
        };
    }

}
