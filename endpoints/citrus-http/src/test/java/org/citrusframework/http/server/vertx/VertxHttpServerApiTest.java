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

package org.citrusframework.http.server.vertx;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Vert.x based Http server must not expose any type that is bound to another server technology.
 * The public API of the server and its builder must be usable without a Jetty, Servlet or Spring MVC
 * classpath.
 */
public class VertxHttpServerApiTest extends AbstractTestNGUnitTest {

    private static final String[] FORBIDDEN_TYPE_PREFIXES = {
            "org.eclipse.jetty",
            "jakarta.servlet",
            "org.springframework.web.servlet"
    };

    @Test
    public void shouldNotExposeJettyTypesOnServer() {
        assertNoForbiddenTypes(VertxHttpServer.class);
    }

    @Test
    public void shouldNotExposeServletTypesOnServer() {
        assertNoForbiddenTypes(VertxHttpServer.class);
    }

    @Test
    public void shouldNotExposeSpringWebMvcTypesOnServer() {
        assertNoForbiddenTypes(VertxHttpServer.class);
    }

    @Test
    public void shouldNotExposeJettyTypesOnBuilder() {
        assertNoForbiddenTypes(VertxHttpServerBuilder.class);
    }

    @Test
    public void shouldNotExposeServletTypesOnBuilder() {
        assertNoForbiddenTypes(VertxHttpServerBuilder.class);
    }

    @Test
    public void shouldNotExposeSpringWebMvcTypesOnBuilder() {
        assertNoForbiddenTypes(VertxHttpServerBuilder.class);
    }

    private void assertNoForbiddenTypes(Class<?> type) {
        for (Constructor<?> constructor : type.getConstructors()) {
            assertTypesAreAllowed(constructor.getParameterTypes(), type.getName() + " constructor");
        }

        for (Method method : type.getMethods()) {
            assertTypesAreAllowed(method.getParameterTypes(), type.getName() + "." + method.getName() + " parameter");
            assertTypesAreAllowed(new Class<?>[]{method.getReturnType()}, type.getName() + "." + method.getName() + " return type");
        }

        for (Field field : type.getFields()) {
            assertTypesAreAllowed(new Class<?>[]{field.getType()}, type.getName() + "." + field.getName() + " field");
        }
    }

    private void assertTypesAreAllowed(Class<?>[] types, String location) {
        for (Class<?> type : types) {
            for (String forbidden : FORBIDDEN_TYPE_PREFIXES) {
                assertThat(type.getName())
                        .as("%s must not use the type %s", location, type.getName())
                        .doesNotStartWith(forbidden);
            }
        }
    }
}
