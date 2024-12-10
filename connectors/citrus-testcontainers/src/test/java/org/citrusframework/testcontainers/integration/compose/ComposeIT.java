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

package org.citrusframework.testcontainers.integration.compose;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.citrusframework.TestAction;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.integration.AbstractTestcontainersIT;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.container.Wait.Builder.waitFor;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class ComposeIT extends AbstractTestcontainersIT {

    @Test
    @CitrusTest
    public void shouldStartContainer() {
        given(doFinally().actions(testcontainers().compose()
                .down()));

        when(testcontainers()
                .compose()
                .up(Resources.create("compose.yaml", ComposeIT.class)));

        then(waitFor().http().url("http://localhost:8880"));

        then(verifyExposedHttpEndpoint());
    }

    private TestAction verifyExposedHttpEndpoint() {
        return new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                try {
                    HttpResponse<String> response = HttpClient.newHttpClient().send(
                            HttpRequest.newBuilder()
                                    .uri(new URI("http://localhost:8880"))
                                    .GET()
                                    .build(), HttpResponse.BodyHandlers.ofString());

                    Assert.assertEquals(response.statusCode(), 200);
                    Assert.assertEquals(response.body(),
                            FileUtils.readToString(Resources.create("html/index.html", ComposeIT.class)));
                } catch (Exception e) {
                    throw new CitrusRuntimeException("Failed to verify exposed Http endpoint", e);
                }
            }
        };
    }
}
