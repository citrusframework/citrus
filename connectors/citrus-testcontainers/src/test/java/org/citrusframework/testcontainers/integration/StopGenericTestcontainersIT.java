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

package org.citrusframework.testcontainers.integration;

import org.citrusframework.annotations.CitrusTest;
import org.testcontainers.containers.GenericContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class StopGenericTestcontainersIT extends AbstractTestcontainersIT {

    @Test
    @CitrusTest
    public void shouldStopContainer() {
        try (GenericContainer<?> busyBox = new GenericContainer("busybox:latest")
                .withCommand("/bin/sh", "-ec", "while :; do echo 'Hello World'; sleep 5 ; done")) {

            given(context -> {
                busyBox.start();
                Assert.assertTrue(busyBox.isRunning());
                context.getReferenceResolver().bind("my-container", busyBox);
            });

            when(testcontainers().stop()
                    .containerName("my-container"));

            then(context -> Assert.assertFalse(busyBox.isRunning()));
        }
    }
}
