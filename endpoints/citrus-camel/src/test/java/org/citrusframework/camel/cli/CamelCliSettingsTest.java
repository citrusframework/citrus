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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class CamelCliSettingsTest {

    @AfterMethod
    public void cleanup() {
        System.clearProperty("citrus.camel.cli.type");
        System.clearProperty("citrus.camel.cli.launcher.jar.path");
    }

    @Test
    public void shouldDefaultToJBangType() {
        Assert.assertEquals(CamelCliSettings.getCliType(), "jbang");
    }

    @Test
    public void shouldReadCliTypeFromSystemProperty() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        Assert.assertEquals(CamelCliSettings.getCliType(), "launcher");
    }

    @Test
    public void shouldReturnNullLauncherJarPathByDefault() {
        Assert.assertNull(CamelCliSettings.getLauncherJarPath());
    }

    @Test
    public void shouldReadLauncherJarPathFromSystemProperty() {
        System.setProperty("citrus.camel.cli.launcher.jar.path", "/opt/camel/camel-cli.jar");
        Assert.assertEquals(CamelCliSettings.getLauncherJarPath(), "/opt/camel/camel-cli.jar");
    }

    @Test
    public void shouldCreateCamelLauncherSupportWhenTypeIsLauncher() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        System.setProperty("citrus.camel.cli.launcher.jar.path", "/opt/camel/camel-cli.jar");

        ProcessLauncher launcher = CamelCliSettings.createLauncher();

        Assert.assertTrue(launcher instanceof CamelLauncherSupport);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldFailWhenLauncherTypeWithoutJarPath() {
        System.setProperty("citrus.camel.cli.type", "launcher");

        CamelCliSettings.createLauncher();
    }

    @Test
    public void shouldFallbackToCamelLauncherJarSystemProperty() {
        System.setProperty("citrus.camel.cli.type", "launcher");
        System.setProperty("camel.launcher.jar", "/opt/camel/fallback.jar");

        try {
            ProcessLauncher launcher = CamelCliSettings.createLauncher();
            Assert.assertTrue(launcher instanceof CamelLauncherSupport);
        } finally {
            System.clearProperty("camel.launcher.jar");
        }
    }
}
