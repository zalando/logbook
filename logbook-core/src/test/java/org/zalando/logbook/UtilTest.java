package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;

import java.io.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by clalleme on 03/04/2016.
 */
public class UtilTest {
    
    @Test
    public void shouldGetByteFromOutputstream() throws Exception {
        Util u = new Util();
        byte[] value = Util.toByteArray(new StringBufferInputStream("This is a test"));
        assertFalse(value == null);
        assertEquals(14, value.length);

    }

    @Test
    public void shouldCopyReaderToWriter() throws Exception {
        StringReader reader = new StringReader("This is a Test");
        StringWriter writer = new StringWriter();

        Util.copy(reader, writer);
        assertThat(writer.toString(), is("This is a Test"));
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionCopyReaderToWriter() throws Exception {
        StringReader reader = new StringReader("This is a Test") {
            @Override
            public int read(final char[] cbuf) throws IOException {
                throw new IOException("This is a fake IOException");
            }
        };
        StringWriter writer = new StringWriter();

        Util.copy(reader, writer);
    }

    @Test
    public void shouldCopyIntputToOutput() throws Exception {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();

        Util.copy(new StringBufferInputStream("This is a Test"), writer);
        assertThat(new String(writer.toByteArray(),"UTF-8"), is("This is a Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalargument() throws Exception {
        Util.checkArgument(true == false, "Fake Error %s", "Foo");
    }

    @Test()
    public void shouldNotThrowIllegalargument() throws Exception {
        Util.checkArgument(true == true, "Fake Error %s", "Foo");
    }

}