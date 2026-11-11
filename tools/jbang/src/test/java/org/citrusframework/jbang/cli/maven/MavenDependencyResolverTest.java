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

package org.citrusframework.jbang.cli.maven;

import org.citrusframework.CitrusVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.jbang.cli.CitrusJBangMain.Settings.CAMEL_VERSION_DEFAULT;

public class MavenDependencyResolverTest {

    @Test
    public void shouldResolveCamelVersionPlaceholder() {
        String gav = "org.apache.camel:camel-aws2-s3:${camel.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assertions.assertEquals("org.apache.camel:camel-aws2-s3:" + CAMEL_VERSION_DEFAULT, resolved);
    }

    @Test
    public void shouldResolveCamelVersionPlaceholderWithCustomVersion() {
        String customVersion = "4.25.0";
        System.setProperty("citrus.camel.jbang.version", customVersion);
        try {
            String gav = "org.apache.camel:camel-kafka:${camel.version}";
            String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
            Assertions.assertEquals("org.apache.camel:camel-kafka:" + customVersion, resolved);
        } finally {
            System.clearProperty("citrus.camel.jbang.version");
        }
    }

    @Test
    public void shouldKeepGavWithoutPlaceholders() {
        String gav = "org.apache.camel:camel-aws2-s3:999-SNAPSHOT";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assertions.assertEquals("org.apache.camel:camel-aws2-s3:999-SNAPSHOT", resolved);
    }

    @Test
    public void shouldHandleNullGav() {
        Assertions.assertNull(MavenDependencyResolver.resolveGavPlaceholders(null));
    }

    @Test
    public void shouldHandleEmptyGav() {
        Assertions.assertEquals("", MavenDependencyResolver.resolveGavPlaceholders(""));
    }

    @Test
    public void shouldResolveCitrusVersionPlaceholder() {
        String gav = "org.citrusframework:citrus-http:${citrus.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assertions.assertEquals("org.citrusframework:citrus-http:" + CitrusVersion.version(), resolved);
    }

    @Test
    public void shouldResolveBothPlaceholders() {
        String gav = "org.apache.camel:camel-kafka:${camel.version}";
        String gav2 = "org.citrusframework:citrus-http:${citrus.version}";

        Assertions.assertEquals("org.apache.camel:camel-kafka:" + CAMEL_VERSION_DEFAULT, MavenDependencyResolver.resolveGavPlaceholders(gav));
        Assertions.assertEquals("org.citrusframework:citrus-http:" + CitrusVersion.version(), MavenDependencyResolver.resolveGavPlaceholders(gav2));
    }

    @Test
    public void shouldKeepUnknownPlaceholders() {
        String gav = "com.example:my-lib:${some.other.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assertions.assertEquals("com.example:my-lib:${some.other.version}", resolved);
    }
}
