/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.arquillian.enricher;

import java.net.URL;

import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.jboss.arquillian.test.api.ArquillianResource;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class ArquillianTest {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint(name = "someEndpoint")
    private Endpoint someEndpoint;

    @CitrusEndpoint(name = "directEndpoint")
    private DirectEndpoint directEndpoint;

    @CitrusEndpoint
    private DirectSyncEndpoint directSyncEndpoint;

    @CitrusTest
    public void testMethod(@CitrusResource TestCaseRunner runner) {
    }

    @CitrusTest
    public void testMethod(@ArquillianResource URL url, @CitrusResource TestCaseRunner runner) {
    }

    @CitrusTest
    public void testMethod(@CitrusResource TestCaseRunner runner, @ArquillianResource URL url) {
    }

    @CitrusTest
    public void testMethod(@ArquillianResource URL url) {
    }

    public void otherMethod() {
    }

    public Citrus getCitrus() {
        return citrus;
    }

    public Endpoint getSomeEndpoint() {
        return someEndpoint;
    }

    public DirectEndpoint getDirectEndpoint() {
        return directEndpoint;
    }

    public DirectSyncEndpoint getDirectSyncEndpoint() {
        return directSyncEndpoint;
    }
}
