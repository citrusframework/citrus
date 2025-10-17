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

public class CamelCustomIT extends TestNGCitrusSupport implements TestActionSupport {

    @Test
    @CitrusTest
    public void runCustomIntegrationWithResourceIT() {

        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }

        given(doFinally().actions(
            catchException().actions(camel().jbang().stop().integration("route"))
        ));

        when(camel().jbang()
                .custom("run", "--name", "route")
                .processName("route")
                .addResource(Resources.fromClasspath("route.yaml", CamelCustomIT.class))
                .withEnv("GREETING", "Hello Camel"));

        then(camel().jbang()
                .verify()
                .integration("route")
                .waitForLogMessage("HELLO CAMEL #10"));
    }
}
