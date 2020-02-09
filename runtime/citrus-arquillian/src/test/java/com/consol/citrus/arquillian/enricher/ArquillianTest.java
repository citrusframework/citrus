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

package com.consol.citrus.arquillian.enricher;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.*;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jms.endpoint.JmsEndpoint;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import org.jboss.arquillian.test.api.ArquillianResource;

import java.net.URL;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class ArquillianTest {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint(name = "someEndpoint")
    private Endpoint someEndpoint;

    @CitrusEndpoint(name = "jmsEndpoint")
    private JmsEndpoint jmsEndpoint;

    @CitrusEndpoint
    private JmsSyncEndpoint jmsSyncEndpoint;

    @CitrusTest
    public void testMethod(@CitrusResource TestDesigner designer) {
    }

    @CitrusTest
    public void testMethod(@ArquillianResource URL url, @CitrusResource TestDesigner designer) {
    }

    @CitrusTest
    public void testMethod(@CitrusResource TestDesigner designer, @ArquillianResource URL url) {
    }

    @CitrusTest
    public void testMethod(@CitrusResource TestRunner runner) {
    }

    @CitrusTest
    public void testMethod(@ArquillianResource URL url, @CitrusResource TestRunner runner) {
    }

    @CitrusTest
    public void testMethod(@CitrusResource TestRunner runner, @ArquillianResource URL url) {
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

    public JmsEndpoint getJmsEndpoint() {
        return jmsEndpoint;
    }

    public JmsSyncEndpoint getJmsSyncEndpoint() {
        return jmsSyncEndpoint;
    }
}
