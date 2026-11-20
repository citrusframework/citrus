/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.jbang.cli.util;

import java.util.Set;

import org.citrusframework.CitrusVersion;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeAnalyzerCatalogVersionTest {

    @Test
    public void shouldResolveCitrusVersion() {
        Assertions.assertEquals(CitrusVersion.version(), new ProbeCodeAnalyzer(CitrusVersion.version()).catalogVersion());
        Assertions.assertFalse(new ProbeCodeAnalyzer(CitrusVersion.version()).catalogVersion().isBlank());
    }

    @Test
    public void shouldRejectBlankCitrusVersion() {
        Assertions.assertThrows(CitrusRuntimeException.class,
                () -> new ProbeCodeAnalyzer("").catalogVersion());
    }

    private static class ProbeCodeAnalyzer implements CodeAnalyzer {

        private final String version;

        private ProbeCodeAnalyzer(String version) {
            this.version = version;
        }

        @Override
        public String citrusVersion() {
            return version;
        }

        @Override
        public Set<String> scanModules(String code) {
            return Set.of();
        }

        @Override
        public Set<String> scanDependencies(String code) {
            return Set.of();
        }

        @Override
        public Set<String> scanTestActions(String code, Set<String> modules) {
            return Set.of();
        }

        @Override
        public Set<String> scanTestContainers(String code, Set<String> modules) {
            return Set.of();
        }

        @Override
        public Set<String> scanEndpoints(String code, Set<String> modules) {
            return Set.of();
        }
    }
}
