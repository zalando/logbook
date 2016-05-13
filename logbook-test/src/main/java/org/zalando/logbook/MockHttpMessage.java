package org.zalando.logbook;

/*
 * #%L
 * Logbook: Test
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

import com.google.common.collect.ListMultimap;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

interface MockHttpMessage {

    default  <K, V> ListMultimap<K, V> firstNonNullNorEmpty(@Nullable final ListMultimap<K, V> first,
            final ListMultimap<K, V> second) {
        return first != null && !first.isEmpty() ? first : requireNonNull(second);
    }

}
