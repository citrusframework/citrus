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

package org.citrusframework.jbang.commands;

import java.nio.file.Path;

import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;

public class InspectTest extends CommandTest {

    @Test
    public void shouldInspectTest() {
        Path source = Resources.fromClasspath("sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("inspect", source.toString());

        Assertions.assertTrue(printer.getOutput().contains("\"name\": \"sample.citrus.it.yaml\""));
        Assertions.assertTrue(printer.getOutput().contains("""
            "modules": [
                "citrus-kafka",
                "citrus-http",
                "citrus-camel",
                "citrus-testcontainers",
                "citrus-base",
                "citrus-jms"
              ],
            """));
        Assertions.assertTrue(printer.getOutput().contains("\"receive\""));
        Assertions.assertTrue(printer.getOutput().contains("\"print\""));
        Assertions.assertTrue(printer.getOutput().contains("\"send\""));
        Assertions.assertTrue(printer.getOutput().contains("\"camel-infra-run\""));
    }
}
