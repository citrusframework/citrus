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

import java.lang.reflect.Method;

import org.citrusframework.http.config.annotation.HttpServerConfig;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The servlet context configuration of the Http server is deprecated as the server
 * implementation is not bound to a Servlet container anymore.
 */
public class HttpServerDeprecationTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldDeprecateContextConfigLocationOnServer() throws NoSuchMethodException {
        assertDeprecated(HttpServer.class.getMethod("getContextConfigLocation"));
        assertDeprecated(HttpServer.class.getMethod("setContextConfigLocation", String.class));
    }

    @Test
    public void shouldDeprecateRootParentContextOnServer() throws NoSuchMethodException {
        assertDeprecated(HttpServer.class.getMethod("isUseRootContextAsParent"));
        assertDeprecated(HttpServer.class.getMethod("setUseRootContextAsParent", boolean.class));
    }

    @Test
    public void shouldDeprecateContextConfigLocationOnBuilder() throws NoSuchMethodException {
        assertDeprecated(AbstractHttpServerBuilder.class.getMethod("contextConfigLocation", String.class));
        assertDeprecated(AbstractHttpServerBuilder.class.getMethod("setContextConfigLocation", String.class));
    }

    @Test
    public void shouldDeprecateRootParentContextOnBuilder() throws NoSuchMethodException {
        assertDeprecated(AbstractHttpServerBuilder.class.getMethod("rootParentContext", boolean.class));
        assertDeprecated(AbstractHttpServerBuilder.class.getMethod("setRootParentContext", boolean.class));
    }

    @Test
    public void shouldDeprecateContextConfigLocationOnAnnotation() throws NoSuchMethodException {
        assertDeprecated(HttpServerConfig.class.getMethod("contextConfigLocation"));
    }

    @Test
    public void shouldDeprecateRootParentContextOnAnnotation() throws NoSuchMethodException {
        assertDeprecated(HttpServerConfig.class.getMethod("rootParentContext"));
    }

    private void assertDeprecated(Method method) {
        Deprecated deprecated = method.getAnnotation(Deprecated.class);

        assertThat(deprecated).as("%s should be deprecated", method).isNotNull();
        assertThat(deprecated.since()).as("%s should be deprecated since 5.2", method).isEqualTo("5.2");
    }
}
