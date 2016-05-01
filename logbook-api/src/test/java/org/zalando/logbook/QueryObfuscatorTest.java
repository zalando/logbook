package org.zalando.logbook;

/*
 * #%L
 * Logbook: API
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
import static org.junit.Assert.*;

public final class QueryObfuscatorTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final QueryObfuscator unit = QueryObfuscator.none();

        assertThat(unit.obfuscate("a=b&c=d&f=e"), is(equalTo("a=b&c=d&f=e")));
    }

    @Test
    public void accessTokenShouldObfuscateAccessTokenParameter() {
        final QueryObfuscator unit = QueryObfuscator.accessToken();

        assertThat(unit.obfuscate("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

    @Test
    public void compoundShouldObfuscateMultipleTimes() {
        final QueryObfuscator unit = QueryObfuscator.compound(
                QueryObfuscator.accessToken(),
                QueryObfuscator.obfuscate("password", "XXX"));

        assertThat(unit.obfuscate("password=s3cr3t&access_token=secure"), is("password=XXX&access_token=XXX"));
    }

}