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

package org.citrusframework.camel.integration;

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.citrusframework.util.TestUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class CamelJBangIT extends TestNGCitrusSupport implements TestActionSupport {

    @Test
    @CitrusTest(name = "RunIntegration_SourceCode_IT")
    public void runIntegrationWithSourceCodeIT() {

        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }

        given(doFinally().actions(
                catchException().actions(camel().jbang().stop().integration("hello"))
        ));

        when(camel().jbang()
                .run("hello", """
                - from:
                    uri: "timer:tick"
                    parameters:
                      period: "1000"
                      includeMetadata: true
                    steps:
                      - setBody:
                          simple: "{{greeting}} #${header.CamelTimerCounter}"
                      - transform:
                          simple: "${body.toUpperCase()}"
                      - to: "log:info"
                """)
                .withSystemProperty("greeting", "Hello Camel"));

        then(camel().jbang()
                .verify()
                .integration("hello")
                .waitForLogMessage("HELLO CAMEL #10"));
    }

    @Test
    @CitrusTest(name = "RunIntegration_Resource_IT")
    public void runIntegrationWithResourceIT() {

        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }

        given(doFinally().actions(
            catchException().actions(camel().jbang().stop().integration("route"))
        ));

        when(camel().jbang()
                .run()
                .integration(Resources.fromClasspath("route.yaml", CamelJBangIT.class))
                .withEnv("GREETING", "Hello Camel"));

        then(camel().jbang()
                .verify()
                .integration("route")
                .waitForLogMessage("HELLO CAMEL #10"));
    }
}
