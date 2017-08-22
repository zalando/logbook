package org.zalando.logbook.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Supplier;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

final class LocalResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private final String protocolVersion;

    private boolean withBody;
    private Tee tee;

    /**
     * All the code connected to this field is to allow for compatibility with Servlet API 2.5
     */
    private int status = 200;

    /**
     * All the code connected to this field is to allow for compatibility with Servlet API 2.5
     */
    private Map<String, List<String>> headers = new HashMap<>();

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
        super(response);
        this.protocolVersion = protocolVersion;
        this.withBody = true;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();
        for (final String header : headers.keySet()) {
            builder.put(header, headers.get(header));
        }
        return builder.build();
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        assertWithBody();
        return this;
    }

    @Override
    public void withoutBody() throws IOException {
        assertWithBody();
        withBody = false;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (withBody) {
            return tee().getOutputStream();
        } else {
            return super.getOutputStream();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (withBody) {
            return tee().getWriter(this::getCharset);
        } else {
            return super.getWriter();
        }
    }

    @Override
    public byte[] getBody() throws IOException {
        assertWithBody();
        return tee().getBytes();
    }

    private void assertWithBody() {
        if (!withBody) {
            throw new IllegalStateException("Response is without body");
        }
    }

    private Tee tee() throws IOException {
        if (tee == null) {
            tee = new Tee(super.getOutputStream());
        }
        return tee;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
        super.setStatus(status);
    }

    @Override
    public void sendError(int status) throws IOException {
        this.status = status;
        super.sendError(status);
    }

    @Override
    public void sendError(int status, String msg) throws IOException {
        this.status = status;
        super.sendError(status, msg);
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        addMultiMapHeader(name, new Date(date).toString()); // TODO: this is dodgy, but in 2.5 we do not have super.getHeader
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        addMultiMapHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        addMultiMapHeader(name, String.valueOf(value));
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        setMultiMapHeader(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        setMultiMapHeader(name, new Date(date).toString()); // TODO: this is dodgy, but in 2.5 we do not have super.getHeader
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        setMultiMapHeader(name, String.valueOf(value));
    }

    private void addMultiMapHeader(String name, String value) {
        List<String> values = headers.computeIfAbsent(name, key -> new ArrayList<>());
        values.add(value);
    }

    private void setMultiMapHeader(String name, String value) {
        headers.remove(name);
        ArrayList<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    private static class Tee {

        private final ByteArrayOutputStream branch;
        private final TeeServletOutputStream output;

        private PrintWriter writer;
        private byte[] bytes;

        private Tee(final OutputStream original) throws IOException {
            this.branch = new ByteArrayOutputStream();
            this.output = new TeeServletOutputStream(original, branch);
        }

        ServletOutputStream getOutputStream() {
            return output;
        }

        PrintWriter getWriter(final Supplier<Charset> charset) {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(output, charset.get()));
            }
            return writer;
        }

        byte[] getBytes() {
            if (bytes == null || bytes.length != branch.size()) {
                bytes = branch.toByteArray();
            }
            return bytes;
        }
    }

    private static class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;
        private final OutputStream branch;

        private TeeServletOutputStream(final OutputStream original, final OutputStream branch) {
            this.original = original;
            this.branch = branch;
        }

        @Override
        public void write(final int b) throws IOException {
            original.write(b);
            branch.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            original.write(b, off, len);
            branch.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
            branch.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
            branch.close();
        }
    }
}
