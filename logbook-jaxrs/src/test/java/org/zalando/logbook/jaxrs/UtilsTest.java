package org.zalando.logbook.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.jaxrs.Utils.copy;
import static org.zalando.logbook.jaxrs.Utils.getPortOptional;
import static org.zalando.logbook.jaxrs.Utils.toByteArray;

public class UtilsTest {

  @Test
  public void copyShouldThrowRequestResponseExceptionOnOoException() throws Exception {
    IOException toThrow = new IOException();
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    when(is.read(any())).thenThrow(toThrow);

    RequestResponseLogProcessingException thrown = assertThrows(
        RequestResponseLogProcessingException.class,
        () -> copy(is, os)
    );
    assertEquals(toThrow, thrown.getCause());
  }

  @Test
  public void toByteArrayShouldConvertInputStream() {
    String input = "This is an input string";
    ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes(UTF_8));
    byte[] result = toByteArray(is);
    assertArrayEquals(input.getBytes(UTF_8), result);
  }

  @Test
  public void getPortOptionalShouldReturnPositivePort() throws Exception {
    URI uri = new URI("http://localhost:99999");
    assertEquals(Optional.of(99999), getPortOptional(uri));
  }

  @Test
  public void getPortOptionalShouldReturnEmptyForNegativePort() throws Exception {
    URI uri = new URI("http://localhost:-99999");
    assertEquals(Optional.empty(), getPortOptional(uri));
  }
}
