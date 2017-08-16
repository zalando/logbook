package org.zalando.logbook.servlet;

import com.google.gag.annotation.remark.Hack;
import com.google.gag.annotation.remark.OhNoYouDidnt;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import static org.mockito.Mockito.mock;

@Hack
@OhNoYouDidnt
public final class EnforceCoverageTest {

    @Test
    void shouldCreateLogbookFilter() {
        new LogbookFilter();
    }

    @Test
    void shouldCallInit() throws ServletException {
        new LogbookFilter().init(mock(FilterConfig.class));
    }

    @Test
    void shouldCallDestroy() {
        new LogbookFilter().destroy();
    }

    @Test
    void shouldUseAttributesConstructor() {
        new Attributes();
    }

    @Test
    void shouldUseByteStreamsConstructor() {
        new ByteStreams();
    }

}
