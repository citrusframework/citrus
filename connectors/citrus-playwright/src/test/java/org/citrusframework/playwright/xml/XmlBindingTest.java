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
package org.citrusframework.playwright.xml;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * JAXB chooses between overloaded setters of one property by reflection order, which the JVM does
 * not specify. A bound setter sharing its name with another setter may then be skipped and its
 * attribute silently dropped on some JVMs only, so this is checked structurally.
 */
class XmlBindingTest {

    @Test
    void shouldNotOverloadBoundSetters() {
        List<Class<?>> wrappers = Arrays.stream(Playwright.class.getMethods())
                .filter(method -> method.isAnnotationPresent(XmlElement.class))
                .map(method -> method.getParameterTypes()[0])
                .distinct()
                .toList();

        List<String> overloaded = wrappers.stream()
                .flatMap(wrapper -> Arrays.stream(wrapper.getMethods())
                        .filter(method -> method.isAnnotationPresent(XmlAttribute.class) || method.isAnnotationPresent(XmlElement.class))
                        .filter(bound -> Arrays.stream(wrapper.getMethods())
                                .filter(method -> method.getName().equals(bound.getName()))
                                .count() > 1)
                        .map(Method::getName)
                        .map(name -> wrapper.getSimpleName() + "#" + name))
                .distinct()
                .collect(Collectors.toList());

        assertTrue(overloaded.isEmpty(), "Overloaded JAXB-bound setters: " + overloaded);
    }
}
