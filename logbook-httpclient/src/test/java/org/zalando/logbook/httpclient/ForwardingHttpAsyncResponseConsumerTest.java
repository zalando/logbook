package org.zalando.logbook.httpclient;

import com.google.gag.annotation.remark.Facepalm;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@OhNoYouDidnt
@Facepalm
@RunWith(MockitoJUnitRunner.class)
public final class ForwardingHttpAsyncResponseConsumerTest {

    @Mock
    private HttpAsyncResponseConsumer delegate;

    private final HttpAsyncResponseConsumer unit = new ForwardingHttpAsyncResponseConsumer() {

        @Override
        protected HttpAsyncResponseConsumer delegate() {
            return delegate;
        }

    };

    @Mock
    private HttpResponse response;

    @Mock
    private ContentDecoder decoder;

    @Mock
    private IOControl control;

    @Mock
    private HttpContext context;

    @Test
    public void testResponseReceived() throws Exception {
        unit.responseReceived(response);
        verify(delegate).responseReceived(response);
    }

    @Test
    public void testConsumeContent() throws Exception {
        unit.consumeContent(decoder, control);
        verify(delegate).consumeContent(decoder, control);
    }

    @Test
    public void testResponseCompleted() throws Exception {
        unit.responseCompleted(context);
        verify(delegate).responseCompleted(context);
    }

    @Test
    public void testCancel() throws Exception {
        unit.cancel();
        verify(delegate).cancel();
    }

    @Test
    public void testIsDone() throws Exception {
        unit.isDone();
        verify(delegate).isDone();
    }

    @Test
    public void testGetResult() throws Exception {
        unit.getResult();
        verify(delegate).getResult();
    }

    @Test
    public void testFailed() throws Exception {
        final IOException e = new IOException();
        unit.failed(e);
        verify(delegate).failed(e);
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void testGetException() throws Exception {
        unit.getException();
        verify(delegate).getException();
    }

    @Test
    public void testClose() throws Exception {
        unit.close();
        verify(delegate).close();
    }
}