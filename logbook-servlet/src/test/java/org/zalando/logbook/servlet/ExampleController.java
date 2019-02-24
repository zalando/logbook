package org.zalando.logbook.servlet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExampleController {

    @RequestMapping("/sync")
    public ResponseEntity<Message> message() {
        final Message message = new Message();
        message.setValue("Hello, world!");
        return ResponseEntity.ok(message);
    }

    @RequestMapping(path = "/echo", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> echo(@RequestBody final String message) {
        return ResponseEntity.ok(message);
    }

    @RequestMapping("/async")
    public Callable<ResponseEntity<Message>> returnMessage() {
        return () -> {
            final Message message = new Message();
            message.setValue("Hello, world!");

            return ResponseEntity.ok(message);
        };
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
        try (final PrintWriter writer = response.getWriter()) {
            copy(request.getReader(), writer);
        }
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
            final Message message = new Message();
            message.setValue("Hello, world!");
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        };
    }

    private static long copy(final Readable from, final Appendable to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        final CharBuffer buf = CharBuffer.allocate(0x800);
        long total = 0;
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            total += buf.remaining();
            buf.clear();
        }
        return total;
    }
}
