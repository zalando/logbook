package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
 * %%
 * Copyright (C) 2015 Zalando SE
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
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.HeaderObfuscator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultHeaderObfuscatorTest extends AbstractTest {

    @Autowired
    private HeaderObfuscator headerHeaderObfuscator;

    @Test
    public void shouldAuthorizationObfuscatorByDefault() {
        assertThat(headerHeaderObfuscator.obfuscate("Authorization", "s3cr3t"), is("XXX"));
        assertThat(headerHeaderObfuscator.obfuscate("X-Access-Token", "s3cr3t"), is("s3cr3t"));
        assertThat(headerHeaderObfuscator.obfuscate("X-Trace-ID", "ABC"), is("ABC"));
    }

}