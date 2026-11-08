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
public class CamelCustomizedRunIntegrationIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void runCustomIntegrationWithResourceIT(@CitrusResource TestCaseRunner runner) {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(),
                "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");

        runner.given(doFinally().actions(
            catchException().actions(camel().cli().stop().integration("route"))
        ));

        runner.when(camel().cli()
                .custom("run", "--name", "route")
                .processName("route")
                .addResource(Resources.fromClasspath("route.yaml", CamelCustomizedRunIntegrationIT.class))
                .withEnv("GREETING", "Hello Camel"));

        runner.then(camel().cli()
                .verify()
                .integration("route")
                .waitForLogMessage("HELLO CAMEL #10"));
    }
}
