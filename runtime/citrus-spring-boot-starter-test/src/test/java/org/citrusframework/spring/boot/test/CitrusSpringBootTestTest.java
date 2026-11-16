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
package org.citrusframework.spring.boot.test;

import java.util.List;

import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

class CitrusSpringBootTestTest {

    @Test
    void composesSpringBootTestAndCitrusSpringSupport() {
        MergedAnnotations annotations = MergedAnnotations.from(DefaultAnnotated.class,
                MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);

        assertThat(annotations.isPresent(SpringBootTest.class)).isTrue();
        assertThat(annotations.isPresent(CitrusSpringSupport.class)).isTrue();

        List<Class<?>> extensions = annotations.stream(ExtendWith.class)
                .flatMap(extendWith -> List.of(extendWith.getClassArray("value")).stream())
                .toList();
        assertThat(extensions).contains(SpringExtension.class, CitrusSpringExtension.class);
    }

    @Test
    void usesSpringBootTestDefaults() {
        SpringBootTest springBootTest = mergedSpringBootTest(DefaultAnnotated.class);

        assertThat(springBootTest.properties()).isEmpty();
        assertThat(springBootTest.args()).isEmpty();
        assertThat(springBootTest.classes()).isEmpty();
        assertThat(springBootTest.webEnvironment()).isEqualTo(SpringBootTest.WebEnvironment.MOCK);
        assertThat(springBootTest.useMainMethod()).isEqualTo(SpringBootTest.UseMainMethod.NEVER);
    }

    @Test
    void forwardsAttributesToSpringBootTest() {
        SpringBootTest springBootTest = mergedSpringBootTest(CustomAnnotated.class);

        assertThat(springBootTest.properties()).containsExactly("foo=bar");
        assertThat(springBootTest.value()).containsExactly("foo=bar");
        assertThat(springBootTest.args()).containsExactly("--debug");
        assertThat(springBootTest.classes()).containsExactly(String.class);
        assertThat(springBootTest.webEnvironment()).isEqualTo(SpringBootTest.WebEnvironment.NONE);
        assertThat(springBootTest.useMainMethod()).isEqualTo(SpringBootTest.UseMainMethod.WHEN_AVAILABLE);
    }

    @Test
    void forwardsValueAsProperties() {
        assertThat(mergedSpringBootTest(ValueAnnotated.class).properties()).containsExactly("foo=bar");
    }

    @Test
    void isInherited() {
        assertThat(mergedSpringBootTest(InheritingClass.class).properties()).containsExactly("foo=bar");
    }

    private static SpringBootTest mergedSpringBootTest(Class<?> type) {
        return MergedAnnotations.from(type, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(SpringBootTest.class)
                .synthesize();
    }

    @CitrusSpringBootTest
    static class DefaultAnnotated {
    }

    @CitrusSpringBootTest(properties = "foo=bar", args = "--debug", classes = String.class,
            webEnvironment = SpringBootTest.WebEnvironment.NONE,
            useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE)
    static class CustomAnnotated {
    }

    @CitrusSpringBootTest("foo=bar")
    static class ValueAnnotated {
    }

    static class InheritingClass extends ValueAnnotated {
    }
}
