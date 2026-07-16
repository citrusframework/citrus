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

import java.nio.file.Path;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LauncherJarCamelLauncherTest {

    @Test
    public void shouldBuildBasicCommand() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        List<String> command = launcher.buildCommand("run", List.of("route.yaml"));

        Assert.assertEquals(command.size(), 3);
        Assert.assertEquals(command.get(2), "java -jar /opt/camel/camel-cli.jar run route.yaml");
    }

    @Test
    public void shouldBuildCommandWithSystemProperties() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");
        launcher.withSystemProperty("camel.cli.version", "4.10.0");

        List<String> command = launcher.buildCommand("run", List.of("route.yaml"));

        Assert.assertEquals(command.size(), 3);
        Assert.assertTrue(command.get(2).contains("-Dcamel.cli.version=\"4.10.0\""));
        Assert.assertTrue(command.get(2).contains("-jar /opt/camel/camel-cli.jar"));
        Assert.assertTrue(command.get(2).endsWith("run route.yaml"));
    }

    @Test
    public void shouldBuildCommandWithMultipleArgs() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        List<String> command = launcher.buildCommand("run", List.of("--name", "myIntegration", "route.yaml"));

        Assert.assertEquals(command.size(), 3);
        Assert.assertEquals(command.get(2), "java -jar /opt/camel/camel-cli.jar run --name myIntegration route.yaml");
    }

    @Test
    public void shouldBuildCommandWithNoArgs() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        List<String> command = launcher.buildCommand("--version", List.of());

        Assert.assertEquals(command.size(), 3);
        Assert.assertEquals(command.get(2), "java -jar /opt/camel/camel-cli.jar --version");
    }

    @Test
    public void shouldBuildStopCommand() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        List<String> command = launcher.buildCommand("stop", List.of("12345"));

        Assert.assertEquals(command.size(), 3);
        Assert.assertEquals(command.get(2), "java -jar /opt/camel/camel-cli.jar stop 12345");
    }

    @Test
    public void shouldUseShellWrapper() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        List<String> command = launcher.buildCommand("ps", List.of());

        Assert.assertEquals(command.size(), 3);
        // On Unix-like systems, first two args are "sh" "-c"
        Assert.assertTrue(command.get(0).equals("sh") || command.get(0).equals("/bin/bash") || command.get(0).equals("cmd.exe"));
    }

    @Test
    public void shouldSetWorkingDir() {
        LauncherJarCamelLauncher launcher = new LauncherJarCamelLauncher("/opt/camel/camel-cli.jar");

        launcher.workingDir(Path.of("/tmp/test"));

        Assert.assertEquals(launcher.getWorkingDir(), Path.of("/tmp/test"));
    }
}
