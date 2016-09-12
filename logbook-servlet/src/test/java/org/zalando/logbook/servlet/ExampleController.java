package org.zalando.logbook.servlet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExampleController {

    @RequestMapping("/sync")
    public ResponseEntity<Message> message() {
        final Message message = new Message();
        message.setValue("Hello, world!");
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

    }

    @RequestMapping("/error")
    public void error() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/read-byte", produces = MediaType.TEXT_PLAIN_VALUE)
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

    @RequestMapping(value = "/read-bytes", produces = MediaType.TEXT_PLAIN_VALUE)
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

    @RequestMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public void stream(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        ByteStreams.copy(request.getInputStream(), response.getOutputStream());
    }

    @RequestMapping(value = "/reader", produces = MediaType.TEXT_PLAIN_VALUE)
    public void reader(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
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
