package org.zalando.logbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface TeeHttpServletResponse extends HttpServletResponse, ReadableBody {

    void finish() throws IOException;

}
