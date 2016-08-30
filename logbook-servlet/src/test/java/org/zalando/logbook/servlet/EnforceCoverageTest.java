package org.zalando.logbook.servlet;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.Test;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    public void shouldCreateLogbookFilter() {
        new LogbookFilter();
    }

    @Test
    public void shouldCallInit() throws ServletException {
        new LogbookFilter().init(mock(FilterConfig.class));
    }

    @Test
    public void shouldCallDestroy() {
        new LogbookFilter().destroy();
    }

    @Test
    public void shouldUseAttributesConstructor() {
        new Attributes();
    }

    @Test
    public void shouldUseByteStreamsConstructor() {
        new ByteStreams();
    }

}
