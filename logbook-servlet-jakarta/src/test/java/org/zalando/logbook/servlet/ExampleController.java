package org.zalando.logbook.servlet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExampleController {

    @RequestMapping("/sync")
    public Message message() {
        return new Message("Hello, world!");
    }

    @RequestMapping(path = "/echo", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> echo(@RequestBody final String message) {
        return ResponseEntity.ok(message);
    }

    @RequestMapping(path = "/async", produces = TEXT_PLAIN_VALUE)
    public Callable<String> returnMessage() {
        return () -> "Hello, world!";
    }

    @RequestMapping("/empty")
    public void empty() {
        // intentionally left blank
    }

    @RequestMapping("/error")
    public void error() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(path = "/read-byte", produces = TEXT_PLAIN_VALUE)
    public void readByte(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final ServletInputStream input = request.getInputStream();
        final ServletOutputStream output = response.getOutputStream();

        while (true) {
            final int read = input.read();
            if (read == -1) {
                break;
            }
            output.write(read);
        }
    }

    @RequestMapping(path = "/read-bytes", produces = TEXT_PLAIN_VALUE)
    public void readBytes(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final ServletInputStream input = request.getInputStream();
        final ServletOutputStream output = response.getOutputStream();

        final byte[] buffer = new byte[1];

        while (true) {
            final int read = input.read(buffer);
            if (read == -1) {
                break;
            }
            output.write(buffer);
        }
    }

    @RequestMapping(path = "/stream", produces = TEXT_PLAIN_VALUE)
    public void stream(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        ByteStreams.copy(request.getInputStream(), response.getOutputStream());
    }

    @RequestMapping(path = "/reader", produces = TEXT_PLAIN_VALUE)
    public void reader(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        copy(request.getReader(), response.getWriter());
    }

    @RequestMapping(path = "/binary", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> binary() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>("Hello", headers, HttpStatus.OK);
    }

    @RequestMapping("/unauthorized")
    public Callable<ResponseEntity<Message>> unauthorized() {
        return () -> {
            final Message message = new Message("Hello, world!");
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        };
    }

    private static void copy(final Readable from, final Appendable to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        final CharBuffer buf = CharBuffer.allocate(0x800);
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            buf.clear();
        }
    }
}
