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

public class CamelLauncherSupportTest {

    public static final String CAMEL_CLI_JAR = "/opt/camel/camel-cli.jar";

    @Test
    public void shouldBuildBasicCommand() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        List<String> command = launcher.buildCommand("run", List.of("route.yaml"));

        Assert.assertEquals(command.size(), 7);
        Assert.assertTrue(String.join(" ", command).endsWith("java -jar %s run route.yaml".formatted(CAMEL_CLI_JAR)));
    }

    @Test
    public void shouldBuildCommandWithSystemProperties() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);
        launcher.withSystemProperty("camel.cli.version", "4.10.0");

        List<String> command = launcher.buildCommand("run", List.of("route.yaml"));

        Assert.assertEquals(command.size(), 8);
        Assert.assertTrue(String.join(" ", command).contains("-Dcamel.cli.version=\"4.10.0\""));
        Assert.assertTrue(String.join(" ", command).contains("-jar %s".formatted(CAMEL_CLI_JAR)));
        Assert.assertTrue(String.join(" ", command).endsWith("run route.yaml"));
    }

    @Test
    public void shouldBuildCommandWithMultipleArgs() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        List<String> command = launcher.buildCommand("run", List.of("--name", "myIntegration", "route.yaml"));

        Assert.assertEquals(command.size(), 9);
        Assert.assertTrue(String.join(" ", command).endsWith("java -jar %s run --name myIntegration route.yaml".formatted(CAMEL_CLI_JAR)));
    }

    @Test
    public void shouldBuildCommandWithNoArgs() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        List<String> command = launcher.buildCommand("--version", List.of());

        Assert.assertEquals(command.size(), 6);
        Assert.assertTrue(String.join(" ", command).endsWith("java -jar %s --version".formatted(CAMEL_CLI_JAR)));
    }

    @Test
    public void shouldBuildStopCommand() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        List<String> command = launcher.buildCommand("stop", List.of("12345"));

        Assert.assertEquals(command.size(), 7);
        Assert.assertTrue(String.join(" ", command).endsWith("java -jar %s stop 12345".formatted(CAMEL_CLI_JAR)));
    }

    @Test
    public void shouldUseShellWrapper() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        List<String> command = launcher.buildCommand("ps", List.of());

        Assert.assertEquals(command.size(), 6);
        Assert.assertTrue(command.get(0).equals("sh") || command.get(0).equals("/bin/bash") || command.get(0).equals("cmd.exe"));
    }

    @Test
    public void shouldSetWorkingDir() {
        CamelLauncherSupport launcher = new CamelLauncherSupport(CAMEL_CLI_JAR);

        launcher.workingDir(Path.of("/tmp/test"));

        Assert.assertEquals(launcher.getWorkingDir(), Path.of("/tmp/test"));
    }
}
