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
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.testing.support.TestModel;
import org.zalando.logbook.jaxrs.testing.support.TestWebService;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Variant;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.BodyReplacers.stream;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;
import static org.zalando.logbook.RawRequestFilters.replaceBody;

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
public final class JerseyClientAndServerTest extends JerseyTest {

    private HttpLogWriter clientWriter;
    private HttpLogWriter serverWriter;

    public JerseyClientAndServerTest() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
    }

    @Override
    protected Application configure() {
        // jersey calls this method within the constructor before our fields are initialized... WTF
        this.clientWriter = mock(HttpLogWriter.class);
        this.serverWriter = mock(HttpLogWriter.class);

        return new ResourceConfig(TestWebService.class)
                .register(new LogbookServerFilter(
                        Logbook.builder()
                                // do not replace multi-part form bodies, which is the default
                                .rawRequestFilter(replaceBody(stream()))
                                .writer(serverWriter)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        super.setUp();

        when(clientWriter.isActive(any())).thenReturn(true);
        when(serverWriter.isActive(any())).thenReturn(true);

        getClient()
                .register(new LogbookClientFilter(
                        Logbook.builder()
                                // do not replace multi-part form bodies, which is the default
                                .rawRequestFilter(replaceBody(stream()))
                                .writer(clientWriter)
                                .build()))
                .register(MultiPartFeature.class);
    }

    @Test
    public void getWithPathAndQueryParamReturningTextPlain() throws Exception {
        final String result = target("testws/testGet/first/textPlain")
                .queryParam("param2", "second")
                .request()
                .get(String.class);

        final RoundTrip roundTrip = getRoundTrip();
        final HttpRequest clientRequest = roundTrip.getClientRequest();
        final HttpResponse clientResponse = roundTrip.getClientResponse();
        final HttpRequest serverRequest = roundTrip.getServerRequest();
        final HttpResponse serverResponse = roundTrip.getServerResponse();

        assertAll(
                () -> assertEquals("param1=first param2=second", result),

                // client request
                () -> assertEquals("GET", clientRequest.getMethod(), "clientRequest method"),
                () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
                () -> assertEquals("localhost", clientRequest.getRemote()),
                () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
                () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
                () -> assertEquals("/testws/testGet/first/textPlain", clientRequest.getPath(), "clientRequest path"),
                () -> assertEquals("param2=second", clientRequest.getQuery(), "clientRequest query"),

                // client response
                () -> assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0),
                        "clientResponse contentType"),
                () -> assertEquals("26", clientResponse.getHeaders().get("Content-length").get(0),
                        "clientResponse contentLength"),
                () -> assertEquals("param1=first param2=second", clientResponse.getBodyAsString(),
                        "client response body"),
                () -> assertEquals("text/plain", clientResponse.getContentType(), "clientResponse contentType"),
                () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
                () -> assertEquals(200, clientResponse.getStatus(), "clientResponse status"),
                () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
                () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

                // server request
                () -> assertEquals("GET", serverRequest.getMethod(), "serverRequest method"),
                () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
                () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
                () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
                () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort()),
                () -> assertEquals("/testws/testGet/first/textPlain", serverRequest.getPath(), "serverRequest path"),
                () -> assertEquals("param2=second", serverRequest.getQuery(), "serverRequest method"),
                () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0),
                        containsString("Jersey/2.27 (HttpUrlConnection")),

                // server response
                () -> assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0),
                        "serverResponse contentType"),
                () -> assertEquals("param1=first param2=second", serverResponse.getBodyAsString(),
                        "server response body"),
                () -> assertEquals("text/plain", serverResponse.getContentType()),
                () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
                () -> assertEquals(200, serverResponse.getStatus()),
                () -> assertEquals(LOCAL, serverResponse.getOrigin()),
                () -> assertEquals(UTF_8, serverResponse.getCharset())
        );
    }

    @Test
    public void multiPartFormDataAndSimulatedFileUpload() throws IOException {
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

        assertAll(
                () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content", result,
                        "result"),

                // client request
                () -> assertEquals("POST", clientRequest.getMethod(), "clientRequest method"),
                () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
                () -> assertEquals("localhost", clientRequest.getRemote()),
                () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
                () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
                () -> assertEquals("/testws/testPostForm", clientRequest.getPath(), "clientRequest path"),
                () -> assertEquals("", clientRequest.getQuery(), "clientRequest query"),
                () -> assertNotEquals("", clientRequest.getBodyAsString(), "client request body"),

                // client response
                () -> assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0),
                        "clientResponse contentType"),
                () -> assertEquals("67", clientResponse.getHeaders().get("Content-length").get(0),
                        "clientResponse contentLength"),
                () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content",
                        clientResponse.getBodyAsString(), "client response body"),
                () -> assertEquals("text/plain", clientResponse.getContentType(), "clientResponse contentType"),
                () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
                () -> assertEquals(200, clientResponse.getStatus(), "clientResponse status"),
                () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
                () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

                // server request
                () -> assertEquals("POST", serverRequest.getMethod(), "serverRequest method"),
                () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
                () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
                () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
                () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort(), "serverRequest port"),
                () -> assertEquals("/testws/testPostForm", serverRequest.getPath(), "serverRequest path"),
                () -> assertEquals("", serverRequest.getQuery(), "serverRequest method"),
                () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0),
                        containsString("Jersey/2.27 (HttpUrlConnection")),
                () -> assertNotEquals("", serverRequest.getBodyAsString(), "server request body"),

                // server response
                () -> assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0),
                        "serverResponse contentType"),
                () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content",
                        serverResponse.getBodyAsString(),
                        "server response body"),
                () -> assertEquals("text/plain", serverResponse.getContentType()),
                () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
                () -> assertEquals(200, serverResponse.getStatus()),
                () -> assertEquals(LOCAL, serverResponse.getOrigin()),
                () -> assertEquals(UTF_8, serverResponse.getCharset())
        );
    }

    @Test
    public void putJsonPayloadReturningJsonPayload() throws IOException {
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

        assertAll(
                // client request
                () -> assertEquals("PUT", clientRequest.getMethod(), "clientRequest method"),
                () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
                () -> assertEquals("localhost", clientRequest.getRemote()),
                () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
                () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
                () -> assertEquals("/testws/testPutJson", clientRequest.getPath(), "clientRequest path"),
                () -> assertEquals("", clientRequest.getQuery(), "clientRequest query"),
                () -> assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", clientRequest.getBodyAsString(),
                        "client request body"),

                // client response
                () -> assertEquals("", clientResponse.getBodyAsString(), "client response body"),
                () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
                () -> assertEquals(204, clientResponse.getStatus(), "clientResponse status"),
                () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
                () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

                // server request
                () -> assertEquals("PUT", serverRequest.getMethod(), "serverRequest method"),
                () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
                () -> assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", serverRequest.getBodyAsString(),
                        "server request body"),
                () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
                () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
                () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
                () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort(), "serverRequest port"),
                () -> assertEquals("/testws/testPutJson", serverRequest.getPath(), "serverRequest path"),
                () -> assertEquals("", serverRequest.getQuery(), "serverRequest method"),
                () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0),
                        containsString("Jersey/2.27 (HttpUrlConnection")),

                // server response
                () -> assertEquals("", serverResponse.getBodyAsString(), "server response body"),
                () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
                () -> assertEquals(204, serverResponse.getStatus()),
                () -> assertEquals(LOCAL, serverResponse.getOrigin()),
                () -> assertEquals(UTF_8, serverResponse.getCharset())
        );
    }

    private RoundTrip getRoundTrip() throws IOException {
        final Correlation<String, String> clientCorrelation = capture(clientWriter);
        final Correlation<String, String> serverCorrelation = capture(serverWriter);

        return new RoundTrip(
                clientCorrelation.getOriginalRequest(),
                clientCorrelation.getOriginalResponse(),
                serverCorrelation.getOriginalRequest(),
                serverCorrelation.getOriginalResponse()
        );
    }

    private static Correlation<String, String> capture(final HttpLogWriter writer) throws IOException {
        @SuppressWarnings("unchecked") final ArgumentCaptor<Correlation<String, String>> captor =
                ArgumentCaptor.forClass(Correlation.class);
        verify(writer).writeResponse(captor.capture());
        return captor.getValue();
    }

    @AfterEach
    public void afterEach() throws Exception {
        super.tearDown();
    }

}
