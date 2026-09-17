package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

abstract class PredicateConfigurationTestSupport {

    @Autowired
    protected Logbook logbook;

    @MockitoBean
    protected HttpLogWriter writer;

    @BeforeEach
    void setUp() {
        doReturn(true).when(writer).isActive();
    }

    protected void assertLogging(
            final String path, final Origin origin, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, "GET", shouldLog);
    }

    protected void assertLogging(
            final String path, final Origin origin, final String method, final boolean shouldLog)
            throws IOException {
        logbook.process(MockHttpRequest.create()
                .withPath(path)
                .withOrigin(origin)
                .withMethod(method)).write();

        verify(writer, shouldLog ? atLeastOnce() : never())
                .write(any(Precorrelation.class), any());
    }

}

@LogbookTest(profiles = "server-client-predicate")
class ServerClientPredicateTest extends PredicateConfigurationTestSupport {

    @ParameterizedTest
    @CsvSource({
            "/api/orders, REMOTE, GET, true",
            "/api/orders, REMOTE, POST, false",
            "/api/private, REMOTE, GET, false",
            "/other, REMOTE, GET, false",
            "/api/private, LOCAL, GET, true",
            "/other, LOCAL, GET, true"
    })
    void shouldApplyPredicatesToTheMatchingRequestOrigin(
            final String path, final Origin origin, final String method, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, method, shouldLog);
    }

}

@LogbookTest(profiles = "only-server-predicate")
class OnlyServerPredicateTest extends PredicateConfigurationTestSupport {

    @ParameterizedTest
    @CsvSource({
            "/api/orders, REMOTE, true",
            "/other, REMOTE, false",
            "/api/orders, LOCAL, true",
            "/other, LOCAL, true"
    })
    void shouldLeaveTheUnconfiguredClientSideUnrestricted(
            final String path, final Origin origin, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, shouldLog);
    }

}

@LogbookTest(profiles = "only-client-predicate")
class OnlyClientPredicateTest extends PredicateConfigurationTestSupport {

    @ParameterizedTest
    @CsvSource({
            "/external/orders, LOCAL, true",
            "/other, LOCAL, false",
            "/external/orders, REMOTE, true",
            "/other, REMOTE, true"
    })
    void shouldLeaveTheUnconfiguredServerSideUnrestricted(
            final String path, final Origin origin, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, shouldLog);
    }

}

@LogbookTest(profiles = "server-predicate-with-global-predicate")
class ServerPredicateWithGlobalPredicateTest extends PredicateConfigurationTestSupport {

    @ParameterizedTest
    @CsvSource({
            "/shared/api/orders, REMOTE, true",
            "/shared/api/blocked, REMOTE, false",
            "/shared/other, REMOTE, false",
            "/other, REMOTE, false",
            "/shared/orders, LOCAL, true",
            "/shared/api/blocked, LOCAL, false",
            "/other, LOCAL, false"
    })
    void shouldCombineTheServerPredicateWithTheGlobalPredicate(
            final String path, final Origin origin, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, shouldLog);
    }

}

@LogbookTest(profiles = "client-predicate-with-global-predicate")
class ClientPredicateWithGlobalPredicateTest extends PredicateConfigurationTestSupport {

    @ParameterizedTest
    @CsvSource({
            "/shared/client/orders, LOCAL, true",
            "/shared/client/blocked, LOCAL, false",
            "/shared/server/orders, LOCAL, false",
            "/other, LOCAL, false",
            "/shared/orders, REMOTE, true",
            "/shared/client/blocked, REMOTE, false",
            "/other, REMOTE, false"
    })
    void shouldCombineTheClientPredicateWithTheGlobalPredicate(
            final String path, final Origin origin, final boolean shouldLog) throws IOException {
        assertLogging(path, origin, shouldLog);
    }

}
