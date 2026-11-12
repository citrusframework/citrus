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

package org.citrusframework.camel.cli;

import org.citrusframework.jbang.ProcessLauncher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Mutates global camel.cli System properties")
public class CamelCliSettingsTest {

    @AfterEach
    public void cleanup() {
        System.clearProperty("citrus.camel.cli.type");
        System.clearProperty("citrus.camel.cli.launcher.jar.path");
        System.clearProperty("citrus.camel.cli.command.restarts");
        System.clearProperty("citrus.camel.cli.receive.restart.grace.period");
    }

    @Test
    public void shouldDefaultToJBangType() {
        Assertions.assertEquals("jbang", CamelCliSettings.getCliType());
    }

    @Test
    public void shouldReadCliTypeFromSystemProperty() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        Assertions.assertEquals("launcher", CamelCliSettings.getCliType());
    }

    @Test
    public void shouldReturnNullLauncherJarPathByDefault() {
        Assertions.assertNull(CamelCliSettings.getLauncherJarPath());
    }

    @Test
    public void shouldReadLauncherJarPathFromSystemProperty() {
        System.setProperty("citrus.camel.cli.launcher.jar.path", "/opt/camel/camel-cli.jar");
        Assertions.assertEquals("/opt/camel/camel-cli.jar", CamelCliSettings.getLauncherJarPath());
    }

    @Test
    public void shouldCreateCamelLauncherSupportWhenTypeIsLauncher() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        System.setProperty("citrus.camel.cli.launcher.jar.path", "/opt/camel/camel-cli.jar");

        ProcessLauncher launcher = CamelCliSettings.createLauncher();

        Assertions.assertTrue(launcher instanceof CamelLauncherSupport);
    }

    @Test
    public void shouldFailWhenLauncherTypeWithoutJarPath() {
        System.setProperty("citrus.camel.cli.type", "launcher");

        Assertions.assertThrows(IllegalStateException.class, CamelCliSettings::createLauncher);
    }

    @Test
    public void shouldFallbackToCamelLauncherJarSystemProperty() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        System.setProperty("camel.launcher.jar", "/opt/camel/fallback.jar");

        try {
            ProcessLauncher launcher = CamelCliSettings.createLauncher();
            Assertions.assertTrue(launcher instanceof CamelLauncherSupport);
        } finally {
            System.clearProperty("camel.launcher.jar");
        }
    }

    @Test
    public void shouldDefaultCommandRestarts() {
        Assertions.assertEquals(1, CamelCliSettings.getCommandRestarts());
    }

    @Test
    public void shouldReadCommandRestartsFromSystemProperty() {
        System.setProperty("citrus.camel.cli.command.restarts", "3");
        Assertions.assertEquals(3, CamelCliSettings.getCommandRestarts());

        System.setProperty("citrus.camel.cli.command.restarts", "-1");
        Assertions.assertEquals(0, CamelCliSettings.getCommandRestarts());
    }

    @Test
    public void shouldDefaultReceiveRestartGracePeriod() {
        Assertions.assertEquals(10000L, CamelCliSettings.getReceiveRestartGracePeriod());
    }

    @Test
    public void shouldReadReceiveRestartGracePeriodFromSystemProperty() {
        System.setProperty("citrus.camel.cli.receive.restart.grace.period", "2500");
        Assertions.assertEquals(2500L, CamelCliSettings.getReceiveRestartGracePeriod());
    }
}
