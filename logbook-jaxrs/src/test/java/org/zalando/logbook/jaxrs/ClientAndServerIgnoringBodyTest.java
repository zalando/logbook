package org.zalando.logbook.jaxrs;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Sink;
import org.zalando.logbook.TestStrategy;
import org.zalando.logbook.jaxrs.testing.support.TestModel;
import org.zalando.logbook.jaxrs.testing.support.TestWebService;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Variant;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.BodyReplacers.stream;
import static org.zalando.logbook.RequestFilters.replaceBody;

final class ClientAndServerIgnoringBodyTest extends JerseyTest {

    private Sink client;
    private Sink server;

    ClientAndServerIgnoringBodyTest() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
    }

    @Override
    protected Application configure() {
        // jersey calls this method within the constructor before our fields are initialized... WTF
        this.client = mock(Sink.class);
        this.server = mock(Sink.class);

        return new ResourceConfig(TestWebService.class)
                .register(new LogbookServerFilter(
                        Logbook.builder()
                                // do not replace multi-part form bodies, which is the default
                                .requestFilter(replaceBody(stream()))
                                .strategy(new TestStrategy())
                                .sink(server)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        super.setUp();

        when(client.isActive()).thenReturn(true);
        when(server.isActive()).thenReturn(true);

        getClient()
                .register(new LogbookClientFilter(
                        Logbook.builder()
                                // do not replace multi-part form bodies, which is the default
                                .requestFilter(replaceBody(stream()))
                                .strategy(new TestStrategy())
                                .sink(client)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @Test
    void getWithPathAndQueryParamReturningTextPlain() throws Exception {
        target("testws/testGet/first/textPlain")
                .queryParam("param2", "second")
                .request()
                .header("Ignore", true)
                .get(String.class);

        final RoundTrip roundTrip = getRoundTrip();

        assertEquals("", roundTrip.getClientRequest().getBodyAsString());
        assertEquals("", roundTrip.getClientResponse().getBodyAsString());
        assertEquals("", roundTrip.getServerRequest().getBodyAsString());
        assertEquals("", roundTrip.getServerResponse().getBodyAsString());
    }

    @Test
    void multiPartFormDataAndSimulatedFileUpload() throws IOException {
        final MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MULTIPART_FORM_DATA_TYPE);
        target("testws/testPostForm")
                .request()
                .header("Ignore", true)
                .post(entity(multiPart.bodyPart(new StreamDataBodyPart("testFileFormField",
                                new ByteArrayInputStream("I am text file content".getBytes(UTF_8)),
                                "testUploadedFilename",
                                TEXT_PLAIN_TYPE
                        ))
                                .bodyPart(new FormDataBodyPart("name", "nameValue!@#$%"))
                                .bodyPart(new FormDataBodyPart("age", "-99")), multiPart.getMediaType()),
                        String.class
                );

        final RoundTrip roundTrip = getRoundTrip();

        assertEquals("", roundTrip.getClientRequest().getBodyAsString());
        assertEquals("", roundTrip.getClientResponse().getBodyAsString());
        assertEquals("", roundTrip.getServerRequest().getBodyAsString());
        assertEquals("", roundTrip.getServerResponse().getBodyAsString());
    }

    @Test
    void putJsonPayloadReturningJsonPayload() throws IOException {
        target("testws/testPutJson")
                .request()
                .header("Ignore", true)
                .put(entity(
                        new TestModel().setProperty1("val1").setProperty2("val2"),
                        new Variant(
                                APPLICATION_JSON_TYPE, Locale.CANADA, "utf-8"
                        ))
                );

        final RoundTrip roundTrip = getRoundTrip();

        assertEquals("", roundTrip.getClientRequest().getBodyAsString());
        assertEquals("", roundTrip.getClientResponse().getBodyAsString());
        assertEquals("", roundTrip.getServerRequest().getBodyAsString());
        assertEquals("", roundTrip.getServerResponse().getBodyAsString());
    }

    private RoundTrip getRoundTrip() throws IOException {
        return new RoundTrip(
                captureRequest(client),
                captureResponse(client),
                captureRequest(server),
                captureResponse(server)
        );
    }

    private static HttpRequest captureRequest(final Sink sink) throws IOException {
        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(sink).write(any(), captor.capture());
        return captor.getValue();
    }

    private static HttpResponse captureResponse(final Sink sink) throws IOException {
        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(sink).write(any(), any(), captor.capture());
        return captor.getValue();
    }

    @AfterEach
    void afterEach() throws Exception {
        super.tearDown();
    }

}
