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
 * Unless required by applicable law or agreed to in write, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.Logbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "logbook")
public final class LogbookProperties {

    private Filter filter = new Filter();
    private Obfuscate obfuscate = new Obfuscate();
    private Write write = new Write();

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Obfuscate getObfuscate() {
        return obfuscate;
    }

    public void setObfuscate(Obfuscate obfuscate) {
        this.obfuscate = obfuscate;
    }

    public Write getWrite() {
        return write;
    }

    public void setWrite(Write write) {
        this.write = write;
    }

    public static class Filter {

        private boolean enabled = true;
        private int order = Ordered.LOWEST_PRECEDENCE;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

    }

    public static class Obfuscate {

        private final List<String> headers = new ArrayList<>(Collections.singleton("Authorization"));
        private final List<String> parameters = new ArrayList<>();

        public List<String> getHeaders() {
            return headers;
        }

        public List<String> getParameters() {
            return parameters;
        }

    }

    public static class Write {

        private String category = Logbook.class.getName();
        private Level level = Level.TRACE;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

    }

}
