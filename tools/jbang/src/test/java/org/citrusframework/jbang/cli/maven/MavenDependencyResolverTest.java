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
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.jbang.cli.CitrusJBangMain.Settings.CAMEL_VERSION_DEFAULT;

public class MavenDependencyResolverTest {

    @Test
    public void shouldResolveCamelVersionPlaceholder() {
        String gav = "org.apache.camel:camel-aws2-s3:${camel.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assert.assertEquals(resolved, "org.apache.camel:camel-aws2-s3:" + CAMEL_VERSION_DEFAULT);
    }

    @Test
    public void shouldResolveCamelVersionPlaceholderWithCustomVersion() {
        String customVersion = "4.25.0";
        System.setProperty("citrus.camel.jbang.version", customVersion);
        try {
            String gav = "org.apache.camel:camel-kafka:${camel.version}";
            String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
            Assert.assertEquals(resolved, "org.apache.camel:camel-kafka:" + customVersion);
        } finally {
            System.clearProperty("citrus.camel.jbang.version");
        }
    }

    @Test
    public void shouldKeepGavWithoutPlaceholders() {
        String gav = "org.apache.camel:camel-aws2-s3:4.21.0";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assert.assertEquals(resolved, "org.apache.camel:camel-aws2-s3:4.21.0");
    }

    @Test
    public void shouldHandleNullGav() {
        Assert.assertNull(MavenDependencyResolver.resolveGavPlaceholders(null));
    }

    @Test
    public void shouldHandleEmptyGav() {
        Assert.assertEquals(MavenDependencyResolver.resolveGavPlaceholders(""), "");
    }

    @Test
    public void shouldResolveCitrusVersionPlaceholder() {
        String gav = "org.citrusframework:citrus-http:${citrus.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assert.assertEquals(resolved, "org.citrusframework:citrus-http:" + CitrusVersion.version());
    }

    @Test
    public void shouldResolveBothPlaceholders() {
        String gav = "org.apache.camel:camel-kafka:${camel.version}";
        String gav2 = "org.citrusframework:citrus-http:${citrus.version}";

        Assert.assertEquals(MavenDependencyResolver.resolveGavPlaceholders(gav),
                "org.apache.camel:camel-kafka:" + CAMEL_VERSION_DEFAULT);
        Assert.assertEquals(MavenDependencyResolver.resolveGavPlaceholders(gav2),
                "org.citrusframework:citrus-http:" + CitrusVersion.version());
    }

    @Test
    public void shouldKeepUnknownPlaceholders() {
        String gav = "com.example:my-lib:${some.other.version}";
        String resolved = MavenDependencyResolver.resolveGavPlaceholders(gav);
        Assert.assertEquals(resolved, "com.example:my-lib:${some.other.version}");
    }
}
