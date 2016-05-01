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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class ObfuscatorsTest {

    @Test
    public void accessTokenShouldObfuscateAccessTokenParameter() {
        final QueryObfuscator unit = Obfuscators.accessToken();

        assertThat(unit.obfuscate("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

    @Test
    public void shouldOnlyObfuscateHeaderIfBothNameAndValueApply() {
        final HeaderObfuscator unit = Obfuscators.obfuscate((name, value) ->
                "name".equals(name) && "Alice".equals(value), "<secret>");

        assertThat(unit.obfuscate("name", "Alice"), is("<secret>"));
        assertThat(unit.obfuscate("name", "Bob"), is("Bob"));
    }

    @Test
    public void authorizationShouldObfuscateAuthorization() {
        final HeaderObfuscator unit = Obfuscators.authorization();

        assertThat(unit.obfuscate("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671"),
                is(equalTo("XXX")));
    }

    @Test
    public void authorizationShouldNotObfuscateNonAuthorization() {
        final HeaderObfuscator unit = Obfuscators.authorization();

        assertThat(unit.obfuscate("Accept", "text/plain"), is(equalTo("text/plain")));
    }

}