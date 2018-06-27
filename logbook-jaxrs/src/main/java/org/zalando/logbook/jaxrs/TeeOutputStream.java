package org.zalando.logbook.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Copies any bytes written to a stream in an internal buffer for later retrieval.
 */
class TeeOutputStream extends FilterOutputStream {
  private final ByteArrayOutputStream copy;

  TeeOutputStream(OutputStream original) {
    super(original);
    this.copy = new ByteArrayOutputStream();
  }

  @Override
  public void flush() throws IOException {
    super.flush();
    copy.flush();
  }

  @Override
  public void close() throws IOException {
    super.close();
    copy.close();
  }

  @Override
  public void write(int b) throws IOException {
    super.write(b);
    copy.write(b);
  }

  public byte[] toByteArray() {
    return copy.toByteArray();
  }
}
