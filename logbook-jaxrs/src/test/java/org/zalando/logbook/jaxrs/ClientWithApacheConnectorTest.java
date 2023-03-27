package org.zalando.logbook.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Sink;
import org.zalando.logbook.jaxrs.testing.support.TestModel;
import org.zalando.logbook.jaxrs.testing.support.TestWebService;

final class ClientWithApacheConnectorTest extends JerseyTest {

    ClientWithApacheConnectorTest() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
    }

    private PoolingHttpClientConnectionManager connectionManager;

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(TestWebService.class)
            .register(MultiPartFeature.class);
        EncodingFilter.enableFor(resourceConfig, GZipEncoder.class);
        return resourceConfig;
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        Sink sink = mock(Sink.class);
        when(sink.isActive()).thenReturn(true);

        connectionManager = new PoolingHttpClientConnectionManager();

        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager)
            .register(new LogbookClientFilter(
                Logbook.builder().sink(sink).build()
            ))
            .connectorProvider(new ApacheConnectorProvider());
    }

    @AfterEach
    void afterEach() {
        connectionManager.shutdown();
    }

    @Test
    void shouldCloseOriginalEntityStreamAndReleaseConnection() {
        target("testws/testGetJson")
            .request(MediaType.APPLICATION_JSON)
            .get(TestModel.class);

        assertEquals(0, connectionManager.getTotalStats().getLeased());
    }
}
