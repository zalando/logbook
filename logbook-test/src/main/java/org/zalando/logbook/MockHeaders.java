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

/*
 * Copyright 2016 Zalando SE.
 *
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
 */
import java.util.List;
import java.util.Map;

public final class MockHeaders {

    MockHeaders() {
        // package private so we can trick code coverage
    }

    public static Map<String, List<String>> of(final String k1, final String v1) {
        return buildHeaders(k1, v1);
    }

    public static Map<String, List<String>> of(final String k1, final String v1, final String k2, final String v2) {
        return buildHeaders(k1, v1, k2, v2);
    }

    public static Map<String, List<String>> of(final String k1, final String v1, final String k2, final String v2, final String k3, final String v3) {
        return buildHeaders(k1, v1, k2, v2, k3, v3);
    }

    private static Map<String, List<String>> buildHeaders(final String... x) {
        final BaseHttpMessage.HeadersBuilder builder = new BaseHttpMessage.HeadersBuilder();
        for (int i = 0; i < x.length; i += 2) {
            builder.put(x[i], x[i + 1]);
        }
        return builder.build();
    }
}
