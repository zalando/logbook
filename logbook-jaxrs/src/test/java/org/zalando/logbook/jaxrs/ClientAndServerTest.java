package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Variant;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Sink;
import org.zalando.logbook.jaxrs.testing.support.TestModel;
import org.zalando.logbook.jaxrs.testing.support.TestWebService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.api.Origin.LOCAL;
import static org.zalando.logbook.api.Origin.REMOTE;
import static org.zalando.logbook.core.BodyReplacers.stream;
import static org.zalando.logbook.core.RequestFilters.replaceBody;

/**
 * This test starts in in-memory server with a Logbook server filter.  The test
 * configures a mock Logbook writer which captures the HttpRequest with HttpResponse objects.
 * The test asserts values on these objects to verify the mapping to/from JAX-RS
 * ContainerRequestContext with ContainerResponseContext is working as expected.
 * <p>
 * Similarly on the client, the test registers a Logbook client filter configured with a mock writer.
 * The test asserts values on the HttpRequest with HttpResponse objects to verify the mapping
 * to/from JAX-RS ClientRequestContext with ClientResponseContext.
 */
final class ClientAndServerTest extends JerseyTest {

    private Sink client;
    private Sink server;

    ClientAndServerTest() {
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
                                .sink(server)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @BeforeEach
    void beforeEach() {

        when(client.isActive()).thenReturn(true);
        when(server.isActive()).thenReturn(true);

        getClient()
                .register(new LogbookClientFilter(
                        Logbook.builder()
                                // do not replace multi-part form bodies, which is the default
                                .requestFilter(replaceBody(stream()))
                                .sink(client)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @Test
    void getWithPathAndQueryParamReturningTextPlain() throws Exception {
        final String result = target("testws/testGet/first/textPlain")
                .queryParam("param2", "second")
                .request()
                .get(String.class);

        final RoundTrip roundTrip = getRoundTrip();
        final HttpRequest clientRequest = roundTrip.getClientRequest();
        final HttpResponse clientResponse = roundTrip.getClientResponse();
        final HttpRequest serverRequest = roundTrip.getServerRequest();
        final HttpResponse serverResponse = roundTrip.getServerResponse();

        assertEquals("param1=first param2=second", result);

        // client request
        assertEquals("HTTP/1.1", clientRequest.getProtocolVersion());
        assertEquals("GET", clientRequest.getMethod());
        assertEquals(LOCAL, clientRequest.getOrigin());
        assertEquals("localhost", clientRequest.getRemote());
        assertEquals("http", clientRequest.getScheme());
        assertEquals("localhost", clientRequest.getHost());
        assertEquals(Optional.of(this.getPort()), clientRequest.getPort());
        assertEquals("/testws/testGet/first/textPlain", clientRequest.getPath());
        assertEquals("param2=second", clientRequest.getQuery());

        // client response
        assertEquals("HTTP/1.1", clientResponse.getProtocolVersion());
        assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0));
        assertEquals("26", clientResponse.getHeaders().get("Content-length").get(0));
        assertEquals("param1=first param2=second", clientResponse.getBodyAsString());
        assertEquals("text/plain", clientResponse.getContentType());
        assertEquals("HTTP/1.1", clientResponse.getProtocolVersion());
        assertEquals(200, clientResponse.getStatus());
        assertEquals(REMOTE, clientResponse.getOrigin());
        assertEquals(UTF_8, clientResponse.getCharset());

        // server request
        assertEquals("HTTP/1.1", serverRequest.getProtocolVersion());
        assertEquals("GET", serverRequest.getMethod());
        assertEquals(REMOTE, serverRequest.getOrigin());
        assertEquals("localhost:" + this.getPort(), serverRequest.getRemote());
        assertEquals("http", serverRequest.getScheme());
        assertEquals("localhost", serverRequest.getHost());
        assertEquals(Optional.of(this.getPort()), serverRequest.getPort());
        assertEquals("/testws/testGet/first/textPlain", serverRequest.getPath());
        assertEquals("param2=second", serverRequest.getQuery());
        assertThat(serverRequest.getHeaders().get("User-Agent"))
                .as("serverRequest userAgent")
                .allSatisfy(userAgent -> assertThat(userAgent).contains("Jersey"));

        // server response
        assertEquals("HTTP/1.1", serverResponse.getProtocolVersion());
        assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0));
        assertEquals("param1=first param2=second", serverResponse.getBodyAsString());
        assertEquals("text/plain", serverResponse.getContentType());
        assertEquals("HTTP/1.1", serverResponse.getProtocolVersion());
        assertEquals(200, serverResponse.getStatus());
        assertEquals(LOCAL, serverResponse.getOrigin());
        assertEquals(UTF_8, serverResponse.getCharset());
    }

    @Test
    void multiPartFormDataAndSimulatedFileUpload() throws IOException {
        final MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MULTIPART_FORM_DATA_TYPE);
        final String result = target("testws/testPostForm").request().post(
                entity(multiPart.bodyPart(new StreamDataBodyPart("testFileFormField",
                        new ByteArrayInputStream("I am text file content".getBytes(UTF_8)),
                        "testUploadedFilename",
                        TEXT_PLAIN_TYPE
                ))
                        .bodyPart(new FormDataBodyPart("name", "nameValue!@#$%"))
                        .bodyPart(new FormDataBodyPart("age", "-99")), multiPart.getMediaType()),
                String.class
        );

        final RoundTrip roundTrip = getRoundTrip();
        final HttpRequest clientRequest = roundTrip.getClientRequest();
        final HttpResponse clientResponse = roundTrip.getClientResponse();
        final HttpRequest serverRequest = roundTrip.getServerRequest();
        final HttpResponse serverResponse = roundTrip.getServerResponse();

        assertEquals("name was nameValue!@#$% age was -99 file was I am text file content", result);

        // client request
        assertEquals("HTTP/1.1", clientRequest.getProtocolVersion());
        assertEquals("POST", clientRequest.getMethod());
        assertEquals(LOCAL, clientRequest.getOrigin());
        assertEquals("localhost", clientRequest.getRemote());
        assertEquals("http", clientRequest.getScheme());
        assertEquals("localhost", clientRequest.getHost());
        assertEquals(Optional.of(this.getPort()), clientRequest.getPort());
        assertEquals("/testws/testPostForm", clientRequest.getPath());
        assertEquals("", clientRequest.getQuery());
        assertNotEquals("", clientRequest.getBodyAsString());

        // client response
        assertEquals("HTTP/1.1", clientResponse.getProtocolVersion());
        assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0));
        assertEquals("67", clientResponse.getHeaders().get("Content-length").get(0));
        assertEquals("name was nameValue!@#$% age was -99 file was I am text file content",
                clientResponse.getBodyAsString());
        assertEquals("text/plain", clientResponse.getContentType());
        assertEquals("HTTP/1.1", clientResponse.getProtocolVersion());
        assertEquals(200, clientResponse.getStatus());
        assertEquals(REMOTE, clientResponse.getOrigin());
        assertEquals(UTF_8, clientResponse.getCharset());

        // server request
        assertEquals("HTTP/1.1", serverRequest.getProtocolVersion());
        assertEquals("POST", serverRequest.getMethod());
        assertEquals(REMOTE, serverRequest.getOrigin());
        assertEquals("localhost:" + this.getPort(), serverRequest.getRemote());
        assertEquals("http", serverRequest.getScheme());
        assertEquals("localhost", serverRequest.getHost());
        assertEquals(Optional.of(this.getPort()), serverRequest.getPort());
        assertEquals("/testws/testPostForm", serverRequest.getPath());
        assertEquals("", serverRequest.getQuery());
        assertThat(serverRequest.getHeaders().get("User-Agent"))
                .as("serverRequest userAgent")
                .allSatisfy(userAgent -> assertThat(userAgent).contains("Jersey"));
        assertNotEquals("", serverRequest.getBodyAsString());

        // server response
        assertEquals("HTTP/1.1", serverResponse.getProtocolVersion());
        assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0));
        assertEquals("name was nameValue!@#$% age was -99 file was I am text file content",
                serverResponse.getBodyAsString());
        assertEquals("text/plain", serverResponse.getContentType());
        assertEquals("HTTP/1.1", serverResponse.getProtocolVersion());
        assertEquals(200, serverResponse.getStatus());
        assertEquals(LOCAL, serverResponse.getOrigin());
        assertEquals(UTF_8, serverResponse.getCharset());
    }

    @Test
    void putJsonPayloadReturningJsonPayload() throws IOException {
        target("testws/testPutJson")
                .request()
                .put(entity(
                        new TestModel().setProperty1("val1").setProperty2("val2"),
                        new Variant(
                                APPLICATION_JSON_TYPE, Locale.CANADA, "utf-8"
                        ))
                );

        final RoundTrip roundTrip = getRoundTrip();
        final HttpRequest clientRequest = roundTrip.getClientRequest();
        final HttpResponse clientResponse = roundTrip.getClientResponse();
        final HttpRequest serverRequest = roundTrip.getServerRequest();
        final HttpResponse serverResponse = roundTrip.getServerResponse();

        // client request
        assertEquals("PUT", clientRequest.getMethod());
        assertEquals(LOCAL, clientRequest.getOrigin());
        assertEquals("localhost", clientRequest.getRemote());
        assertEquals("http", clientRequest.getScheme());
        assertEquals("localhost", clientRequest.getHost());
        assertEquals(Optional.of(this.getPort()), clientRequest.getPort());
        assertEquals("/testws/testPutJson", clientRequest.getPath());
        assertEquals("", clientRequest.getQuery());
        assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", clientRequest.getBodyAsString());

        // client response
        assertEquals("", clientResponse.getBodyAsString());
        assertEquals("HTTP/1.1", clientResponse.getProtocolVersion());
        assertEquals(204, clientResponse.getStatus());
        assertEquals(REMOTE, clientResponse.getOrigin());
        assertEquals(UTF_8, clientResponse.getCharset());

        // server request
        assertEquals("PUT", serverRequest.getMethod());
        assertEquals(REMOTE, serverRequest.getOrigin());
        assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", serverRequest.getBodyAsString());
        assertEquals("localhost:" + this.getPort(), serverRequest.getRemote());
        assertEquals("http", serverRequest.getScheme());
        assertEquals("localhost", serverRequest.getHost());
        assertEquals(Optional.of(this.getPort()), serverRequest.getPort());
        assertEquals("/testws/testPutJson", serverRequest.getPath());
        assertEquals("", serverRequest.getQuery());
        assertThat(serverRequest.getHeaders().get("User-Agent"))
                .as("serverRequest userAgent")
                .allSatisfy(userAgent -> assertThat(userAgent).contains("Jersey"));

        // server response
        assertEquals("", serverResponse.getBodyAsString());
        assertEquals("HTTP/1.1", serverResponse.getProtocolVersion());
        assertEquals(204, serverResponse.getStatus());
        assertEquals(LOCAL, serverResponse.getOrigin());
        assertEquals(UTF_8, serverResponse.getCharset());
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

}
