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

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.TestUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Fresh Spring context per run registers duplicate camelContext JMX MBean under parallel classes")
@CitrusSupport
public class CamelCliIT implements TestActionSupport {

    @Test
    @CitrusTest(name = "RunIntegration_SourceCode_IT")
    public void runIntegrationWithSourceCodeIT(@CitrusResource TestCaseRunner runner) {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(),
                "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");

        runner.given(doFinally().actions(
                catchException().actions(camel().cli().stop().integration("hello"))
        ));

        runner.when(camel().cli()
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

        runner.then(camel().cli()
                .verify()
                .integration("hello")
                .waitForLogMessage("HELLO CAMEL #10"));
    }

    @Test
    @CitrusTest(name = "CmdSendMessage_IT")
    public void cmdSendMessageIT(@CitrusResource TestCaseRunner runner) {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(),
                "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");

        runner.given(doFinally().actions(
                catchException().actions(camel().cli().stop().integration("echo"))
        ));

        runner.given(camel().cli()
                .run("echo", """
                - from:
                    uri: "direct:echo"
                    steps:
                      - transform:
                          simple: "${body.toUpperCase()}"
                      - to: "log:info"
                """))
        .and(camel().cli()
                .verify()
                .integration("echo")
                .waitForLogMessage("Started route1 (direct://echo)"));

        runner.when(camel().cli()
                    .cmd()
                    .send()
                    .integration("echo")
                    .body("Hello Camel"));

        runner.then(camel().cli()
                .verify()
                .integration("echo")
                .waitForLogMessage("HELLO CAMEL"));

        runner.when(camel().cli()
                    .cmd()
                    .send()
                    .integration("echo")
                    .endpoint("direct:echo")
                    .body("Camel rocks!"));

        runner.then(camel().cli()
                .verify()
                .integration("echo")
                .waitForLogMessage("CAMEL ROCKS!"));

        runner.when(camel().cli()
                    .cmd()
                    .send()
                    .integration("echo")
                    .endpointUri("direct:echo")
                    .reply(true)
                    .withArg("--show-headers")
                    .header("foo", "bar")
                    .body("Good Bye Camel!"));

        runner.then(camel().cli()
                .verify()
                .integration("echo")
                .waitForLogMessage("GOOD BYE CAMEL!"));
    }

    @Test
    @CitrusTest(name = "RunIntegration_Resource_IT")
    public void runIntegrationWithResourceIT(@CitrusResource TestCaseRunner runner) {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(),
                "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");

        runner.given(doFinally().actions(
            catchException().actions(camel().cli().stop().integration("route"))
        ));

        runner.when(camel().cli()
                .run()
                .integration(Resources.fromClasspath("route.yaml", CamelCliIT.class))
                .withEnv("GREETING", "Hello Camel"));

        runner.then(camel().cli()
                .verify()
                .integration("route")
                .waitForLogMessage("HELLO CAMEL #10"));
    }
}
