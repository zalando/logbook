package org.zalando.logbook.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Variant;

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
import org.mockito.Captor;
import org.zalando.logbook.BodyReplacers;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.testing.support.TestModel;
import org.zalando.logbook.jaxrs.testing.support.TestWebService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;
import static org.zalando.logbook.RawRequestFilters.replaceBody;

/**
 * This test starts in in-memory server with a Logbook server filter.  The test
 * configures a mock Logbook writer which captures the HttpRequest with HttpResponse objects.
 * The test asserts values on these objects to verify the mapping to/from JAX-RS
 * ContainerRequestContext with ContainerResponseContext is working as expected.
 *
 * Similarly on the client, the test registers a Logbook client filter configured with a mock writer.
 * The test asserts values on the HttpRequest with HttpResponse objects to verify the mapping
 * to/from JAX-RS ClientRequestContext with ClientResponseContext.
 */
public class JerseyClientAndServerTest extends JerseyTest {

  @Captor
  private ArgumentCaptor<Correlation<String, String>> serverCorrelationCaptor;
  @Captor
  private ArgumentCaptor<Correlation<String, String>> clientCorrelationCaptor;
  private HttpLogWriter serverMockWriter;
  private HttpLogWriter clientMockWriter;
  private HttpRequest clientRequest;
  private HttpResponse clientResponse;
  private HttpRequest serverRequest;
  private HttpResponse serverResponse;


  public JerseyClientAndServerTest() {
    super();
    // Force the test server to choose an available port, rather than the default 9998
    forceSet(TestProperties.CONTAINER_PORT, "0");
  }

  private void setupMocks() {
    try {
      serverMockWriter = mock(HttpLogWriter.class);
      clientMockWriter = mock(HttpLogWriter.class);
      when(clientMockWriter.isActive(any())).thenReturn(true);
      when(serverMockWriter.isActive(any())).thenReturn(true);

    } catch (IOException ex) {
      throw new UnitTestSetupException(ex);
    }
  }

  private void populateRequestsAndResponsesFromMockCaptors() throws IOException {
    verify(clientMockWriter).writeResponse(clientCorrelationCaptor.capture());
    verify(serverMockWriter).writeResponse(serverCorrelationCaptor.capture());

    Correlation<String,String> clientCorrelation = clientCorrelationCaptor.getValue();
    Correlation<String,String> serverCorrelation = serverCorrelationCaptor.getValue();
    clientRequest = clientCorrelation.getOriginalRequest();
    clientResponse = clientCorrelation.getOriginalResponse();
    serverRequest = serverCorrelation.getOriginalRequest();
    serverResponse = serverCorrelation.getOriginalResponse();
  }

  @Override
  protected Application configure() {
    initMocks(this);
    setupMocks();

    return new ResourceConfig(TestWebService.class)
        .register(
            new ServerLoggingFilter(
                Logbook.builder()
                       // do not replace multi-part form bodies, which is the default
                       .rawRequestFilter(replaceBody(BodyReplacers.stream()))
                       // use our "writer" to capture the request with response objects on the server
                       .writer(serverMockWriter)
                       .build()
            )
        ).register(MultiPartFeature.class);
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    super.setUp();

    getClient().register(
        new ClientLoggingFilter(
            Logbook.builder()
                   // do not replace multi-part form bodies, which is the default
                   .rawRequestFilter(replaceBody(BodyReplacers.stream()))
                   // use our "writer" to capture the request with response objects on the client
                   .writer(clientMockWriter)
                   .build()))
               .register(MultiPartFeature.class);
  }

  @AfterEach
  public void afterEach() throws Exception {
    super.tearDown();
  }

  @Test
  public void getWithPathAndQueryParamReturningTextPlain() throws Exception {
    final String result = target("testws/testGet/first/textPlain")
        .queryParam("param2", "second")
        .request()
        .get(String.class);

    populateRequestsAndResponsesFromMockCaptors();

    assertAll(
        () -> assertEquals("param1=first param2=second", result),

        // clientRequest
        () -> assertEquals("GET", clientRequest.getMethod(), "clientRequest method"),
        () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
        () -> assertEquals("localhost", clientRequest.getRemote()),
        () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
        () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
        () -> assertEquals("/testws/testGet/first/textPlain", clientRequest.getPath(), "clientRequest path"),
        () -> assertEquals("param2=second", clientRequest.getQuery(), "clientRequest query"),
        () -> assertThat("clientRequest userAgent", clientRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // clientResponse
        () -> assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0), "clientResponse contentType"),
        () -> assertEquals("26", clientResponse.getHeaders().get("Content-length").get(0), "clientResponse contentLength"),
        () -> assertEquals("param1=first param2=second", clientResponse.getBodyAsString(), "clientResponse body"),
        () -> assertEquals("text/plain", clientResponse.getContentType(), "clientResponse contentType"),
        () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
        () -> assertEquals(200, clientResponse.getStatus(), "clientResponse status"),
        () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
        () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

        // serverRequest
        () -> assertEquals("GET", serverRequest.getMethod(), "serverRequest method"),
        () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
        () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
        () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
        () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort()),
        () -> assertEquals("/testws/testGet/first/textPlain", serverRequest.getPath(), "serverRequest path"),
        () -> assertEquals("param2=second", serverRequest.getQuery(), "serverRequest method"),
        () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // serverResponse
        () -> assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0), "serverResponse contentType"),
        () -> assertEquals("param1=first param2=second", serverResponse.getBodyAsString()),
        () -> assertEquals("text/plain", serverResponse.getContentType()),
        () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
        () -> assertEquals(200, serverResponse.getStatus()),
        () -> assertEquals(LOCAL, serverResponse.getOrigin()),
        () -> assertEquals(UTF_8, serverResponse.getCharset())
    );
  }

  @Test
  public void multiPartFormDataAndSimulatedFileUpload() throws IOException {
    MultiPart multiPart = new MultiPart();
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
    populateRequestsAndResponsesFromMockCaptors();

    assertAll(
        () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content", result),

        // clientRequest
        () -> assertEquals("POST", clientRequest.getMethod(), "clientRequest method"),
        () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
        () -> assertEquals("localhost", clientRequest.getRemote()),
        () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
        () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
        () -> assertEquals("/testws/testPostForm", clientRequest.getPath(), "clientRequest path"),
        () -> assertEquals("", clientRequest.getQuery(), "clientRequest query"),
        () -> assertThat("clientRequest userAgent", clientRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // clientResponse
        () -> assertEquals("text/plain", clientResponse.getHeaders().get("Content-type").get(0), "clientResponse contentType"),
        () -> assertEquals("67", clientResponse.getHeaders().get("Content-length").get(0), "clientResponse contentLength"),
        () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content", clientResponse.getBodyAsString(), "clientResponse body"),
        () -> assertEquals("text/plain", clientResponse.getContentType(), "clientResponse contentType"),
        () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
        () -> assertEquals(200, clientResponse.getStatus(), "clientResponse status"),
        () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
        () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

        // serverRequest
        () -> assertEquals("POST", serverRequest.getMethod(), "serverRequest method"),
        () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
        () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
        () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
        () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort(), "serverRequest port"),
        () -> assertEquals("/testws/testPostForm", serverRequest.getPath(), "serverRequest path"),
        () -> assertEquals("", serverRequest.getQuery(), "serverRequest method"),
        () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // serverResponse
        () -> assertEquals("text/plain", serverResponse.getHeaders().get("Content-type").get(0), "serverResponse contentType"),
        () -> assertEquals("name was nameValue!@#$% age was -99 file was I am text file content", serverResponse.getBodyAsString()),
        () -> assertEquals("text/plain", serverResponse.getContentType()),
        () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
        () -> assertEquals(200, serverResponse.getStatus()),
        () -> assertEquals(LOCAL, serverResponse.getOrigin()),
        () -> assertEquals(UTF_8, serverResponse.getCharset())
    );
  }

  @Test
  public void putJsonPayloadReturningJsonPayload() throws IOException {

    final TestModel result = target("testws/testPutJson")
        .request()
        .put(entity(
            new TestModel().setProperty1("val1").setProperty2("val2"),
            new Variant(
                APPLICATION_JSON_TYPE, Locale.CANADA, "utf-8"
            )),
            TestModel.class
        );
    populateRequestsAndResponsesFromMockCaptors();

    assertAll(
        () -> assertEquals("val1", result.getProperty1()),
        () -> assertEquals("val2", result.getProperty2()),

        // clientRequest
        () -> assertEquals("PUT", clientRequest.getMethod(), "clientRequest method"),
        () -> assertEquals(LOCAL, clientRequest.getOrigin(), "clientRequest origin"),
        () -> assertEquals("localhost", clientRequest.getRemote()),
        () -> assertEquals("http", clientRequest.getScheme(), "clientRequest scheme"),
        () -> assertEquals("localhost", clientRequest.getHost(), "clientRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), clientRequest.getPort()),
        () -> assertEquals("/testws/testPutJson", clientRequest.getPath(), "clientRequest path"),
        () -> assertEquals("", clientRequest.getQuery(), "clientRequest query"),
        () -> assertThat("clientRequest userAgent", clientRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // clientResponse
        () -> assertEquals("application/json", clientResponse.getHeaders().get("Content-type").get(0), "clientResponse contentType"),
        () -> assertEquals("39", clientResponse.getHeaders().get("Content-length").get(0), "clientResponse contentLength"),
        () -> assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", clientResponse.getBodyAsString(), "clientResponse body"),
        () -> assertEquals("application/json", clientResponse.getContentType(), "clientResponse contentType"),
        () -> assertEquals("HTTP/1.1", clientResponse.getProtocolVersion(), "clientResponse protocolVersion"),
        () -> assertEquals(200, clientResponse.getStatus(), "clientResponse status"),
        () -> assertEquals(REMOTE, clientResponse.getOrigin(), "clientResponse origin"),
        () -> assertEquals(UTF_8, clientResponse.getCharset(), "clientResponse charset"),

        // serverRequest
        () -> assertEquals("PUT", serverRequest.getMethod(), "serverRequest method"),
        () -> assertEquals(REMOTE, serverRequest.getOrigin(), "serverRequest origin"),
        () -> assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", serverRequest.getBodyAsString()),
        () -> assertEquals("localhost:" + this.getPort(), serverRequest.getRemote()),
        () -> assertEquals("http", serverRequest.getScheme(), "serverRequest scheme"),
        () -> assertEquals("localhost", serverRequest.getHost(), "serverRequest host"),
        () -> assertEquals(Optional.of(this.getPort()), serverRequest.getPort(), "serverRequest port"),
        () -> assertEquals("/testws/testPutJson", serverRequest.getPath(), "serverRequest path"),
        () -> assertEquals("", serverRequest.getQuery(), "serverRequest method"),
        () -> assertThat("serverRequest userAgent", serverRequest.getHeaders().get("User-Agent").get(0), containsString("Jersey/2.27 (HttpUrlConnection")),

        // serverResponse
        () -> assertEquals("application/json", serverResponse.getHeaders().get("Content-type").get(0), "serverResponse contentType"),
        () -> assertEquals("{\"property1\":\"val1\",\"property2\":\"val2\"}", serverResponse.getBodyAsString()),
        () -> assertEquals("application/json", serverResponse.getContentType()),
        () -> assertEquals("HTTP/1.1", serverResponse.getProtocolVersion()),
        () -> assertEquals(200, serverResponse.getStatus()),
        () -> assertEquals(LOCAL, serverResponse.getOrigin()),
        () -> assertEquals(UTF_8, serverResponse.getCharset())
    );
  }
}
