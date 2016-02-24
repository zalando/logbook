package org.zalando.logbook.undertow;

/*
 * #%L
 * Logbook: Undertow
 * %%
 * Copyright (C) 2016 Zalando SE
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.RawHttpRequest;

import com.google.common.collect.ImmutableList;

public class CapturingLogbook implements Logbook {

    private final List<CapturedCorrelation> captures = new CopyOnWriteArrayList<>();

    private volatile boolean active = true;

    @Override
    public Optional<Correlator> write(final RawHttpRequest request) throws IOException {
        if (!active) {
            return Optional.empty();
        }

        final CapturedCorrelation correlation = new CapturedCorrelation(request.withBody());
        captures.add(correlation);
        return Optional.of(correlation);
    }

    public List<CapturedCorrelation> getCaptures() {
        return ImmutableList.copyOf(captures);
    }

    public CapturingLogbook activate() {
        active = true;
        return this;
    }

    public CapturingLogbook deactivate() {
        active = false;
        return this;
    }

}
