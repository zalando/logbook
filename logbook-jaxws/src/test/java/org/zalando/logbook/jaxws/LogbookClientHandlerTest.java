package org.zalando.logbook.jaxws;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LogbookClientHandlerTest<Client extends BookService & BindingProvider> {

    private final Infrastructure infrastructure = new Infrastructure();
    private final Client client = infrastructure.client();

    public LogbookClientHandlerTest() {
        infrastructure.register(client.getBinding(), LogbookClientHandler::new);
    }

    @BeforeEach
    void start() {
        infrastructure.start();
    }

    @AfterEach
    void stop() {
        infrastructure.stop();
    }

    @Test
    void receivesResponse() {
        final Book book = client.getBook(1);

        assertThat(book.getId()).isEqualTo(1);
        assertThat(book.getName()).isEqualTo("Logbook");
    }

    @Test
    void logsRequests() throws IOException {
        client.getBook(1);

        final HttpRequest request = infrastructure.request();

        assertThat(request.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(request.getOrigin()).isEqualTo(Origin.LOCAL);
        assertThat(request.getRemote()).isEqualTo("localhost");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getRequestUri()).isEqualTo("http://localhost:8080/books");
        assertThat(request.getScheme()).isEqualTo("http");
        assertThat(request.getHost()).isEqualTo("localhost");
        assertThat(request.getPort()).contains(8080);
        assertThat(request.getPath()).isEqualTo("/books");
        assertThat(request.getQuery()).isEmpty();
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getContentType()).isNull();
        assertThat(request.getCharset()).isEqualTo(UTF_8);
        assertThat(request.getBodyAsString())
                .contains("Envelope")
                .contains("Header")
                .contains("Body")
                .contains("getBook")
                .contains("<bookId>1</bookId>");
    }

    @Test
    void logsResponses() throws IOException {
        client.getBook(1);

        final HttpResponse response = infrastructure.response();

        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(response.getOrigin()).isEqualTo(Origin.REMOTE);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaders()).isNotNull();
        assertThat(response.getContentType()).isNull();
        assertThat(response.getCharset()).isEqualTo(UTF_8);
        assertThat(response.getBodyAsString())
                .contains("Envelope")
                .contains("Header")
                .contains("Body")
                .contains("getBookResponse")
                .contains("<id>1</id>")
                .contains("<name>Logbook</name>");
    }

    @Test
    void logsFaults() throws IOException {
        assertThrows(SOAPFaultException.class, () -> client.getBook(-1));

        final HttpResponse response = infrastructure.response();

        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(response.getOrigin()).isEqualTo(Origin.REMOTE);
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getHeaders()).isNotNull();
        assertThat(response.getContentType()).isNull();
        assertThat(response.getCharset()).isEqualTo(UTF_8);
        assertThat(response.getBodyAsString())
                .contains("Envelope")
                .contains("Header")
                .contains("Body")
                .contains("Fault")
                .contains("<faultcode>S:Server</faultcode>")
                .contains("<faultstring>Invalid book identifier</faultstring>");
    }

}
