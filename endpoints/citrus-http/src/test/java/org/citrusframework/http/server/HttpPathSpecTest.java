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

package org.citrusframework.http.server;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the path specification used to match request paths against secured paths.
 */
public class HttpPathSpecTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldMatchExactPath() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("/orders");

        assertThat(pathSpec.getType()).isEqualTo(HttpPathSpec.Type.EXACT);
        assertThat(pathSpec.matches("/orders")).isTrue();
        assertThat(pathSpec.matches("/orders/42")).isFalse();
        assertThat(pathSpec.matches("/other")).isFalse();
    }

    @Test
    public void shouldMatchPrefixPath() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("/secured/*");

        assertThat(pathSpec.getType()).isEqualTo(HttpPathSpec.Type.PREFIX);
        assertThat(pathSpec.matches("/secured")).isTrue();
        assertThat(pathSpec.matches("/secured/orders")).isTrue();
        assertThat(pathSpec.matches("/securedfoo")).isFalse();
        assertThat(pathSpec.matches("/other")).isFalse();
    }

    @Test
    public void shouldMatchPrefixPathWithEmptyPrefix() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("/*");

        assertThat(pathSpec.matches("/orders")).isTrue();
        assertThat(pathSpec.matches("/orders/42")).isTrue();
        assertThat(pathSpec.matches("orders")).isFalse();
    }

    @Test
    public void shouldMatchSuffixPath() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("*.json");

        assertThat(pathSpec.getType()).isEqualTo(HttpPathSpec.Type.SUFFIX);
        assertThat(pathSpec.matches("/orders.json")).isTrue();
        assertThat(pathSpec.matches("/orders.xml")).isFalse();
    }

    @Test
    public void shouldMatchDefaultPath() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("/");

        assertThat(pathSpec.getType()).isEqualTo(HttpPathSpec.Type.DEFAULT);
        assertThat(pathSpec.matches("/orders")).isTrue();
        assertThat(pathSpec.matches("/")).isTrue();
    }

    @Test
    public void shouldMatchRootPath() {
        HttpPathSpec pathSpec = HttpPathSpec.parse("");

        assertThat(pathSpec.getType()).isEqualTo(HttpPathSpec.Type.ROOT);
        assertThat(pathSpec.matches("/")).isTrue();
        assertThat(pathSpec.matches("/orders")).isFalse();
    }

    @Test
    public void shouldNotMatchNullPath() {
        assertThat(HttpPathSpec.parse("/orders").matches(null)).isFalse();
    }

    @Test
    public void shouldOrderBySpecificity() {
        HttpPathSpec exact = HttpPathSpec.parse("/secured/orders");
        HttpPathSpec prefix = HttpPathSpec.parse("/secured/*");
        HttpPathSpec suffix = HttpPathSpec.parse("*.json");
        HttpPathSpec defaultPath = HttpPathSpec.parse("/");

        assertThat(exact.getSpecificity()).isGreaterThan(prefix.getSpecificity());
        assertThat(prefix.getSpecificity()).isGreaterThan(suffix.getSpecificity());
        assertThat(suffix.getSpecificity()).isGreaterThan(defaultPath.getSpecificity());
    }

    @Test
    public void shouldPreferLongerPrefix() {
        assertThat(HttpPathSpec.parse("/secured/orders/*").getSpecificity())
                .isGreaterThan(HttpPathSpec.parse("/secured/*").getSpecificity());
    }
}
