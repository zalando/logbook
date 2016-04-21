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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.zalando.logbook.DefaultHttpLogWriter.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "logbook")
public final class LogbookProperties {

    private final List<String> exclude = new ArrayList<>();
    private final Obfuscate obfuscate = new Obfuscate();
    private final Write write = new Write();

    public List<String> getExclude() {
        return exclude;
    }

    public Obfuscate getObfuscate() {
        return obfuscate;
    }

    public Write getWrite() {
        return write;
    }

    public static class Obfuscate {

        private final List<String> headers = new ArrayList<>();
        private final List<String> parameters = new ArrayList<>();

        public List<String> getHeaders() {
            return headers;
        }

        public List<String> getParameters() {
            return parameters;
        }

    }

    public static class Write {

        private String category;
        private Level level;

        @Nullable
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        @Nullable
        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

    }

}
