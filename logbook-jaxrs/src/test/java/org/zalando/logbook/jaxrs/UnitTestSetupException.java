package org.zalando.logbook.jaxrs;

public class UnitTestSetupException extends RuntimeException {

    public UnitTestSetupException(Exception ex) {
        super(ex);
    }
}
