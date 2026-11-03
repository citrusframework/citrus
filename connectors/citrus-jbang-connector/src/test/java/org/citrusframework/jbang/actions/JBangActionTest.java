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

package org.citrusframework.jbang.actions;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JBangActionTest extends AbstractUnitTest {

    private final Resource helloScript = Resources.fromClasspath("org/citrusframework/jbang/hello.java");

    @BeforeAll
    public static void beforeClass() {
        if (!TestUtils.isNetworkReachable()) {
            Assumptions.abort("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }
    }

    @Test
    public void testScriptOrFile() {
        JBangAction jbang = new JBangAction.Builder()
                .file(helloScript)
                .build();
        jbang.execute(context);
    }

    @Test
    public void testVerifyOutput() {
        JBangAction jbang = new JBangAction.Builder()
                .file(helloScript)
                .arg("Citrus")
                .verifyOutput("Hello Citrus")
                .build();
        jbang.execute(context);
    }

    @Test
    public void testValidationProcessor() {
        JBangAction jbang = new JBangAction.Builder()
                .file(helloScript)
                .arg("Citrus")
                .verifyOutput((message, context) -> {
                    Assertions.assertEquals("Hello Citrus", message.getPayload(String.class));
                    Assertions.assertEquals(0, message.getHeader("exitCode"));
                    Assertions.assertTrue(message.getHeaders().containsKey("pid"));
                    Assertions.assertFalse(message.getHeader("pid").toString().isEmpty());
                })
                .build();
        jbang.execute(context);
    }

    @Test
    public void testJBangCommand() {
        JBangAction jbang = new JBangAction.Builder().command("version").build();
        jbang.execute(context);
    }

    @Test
    public void testJBangCommandSavePid() {
        JBangAction jbang = new JBangAction.Builder().command("version")
                .savePid("versionPid")
                .build();
        jbang.execute(context);

        Assertions.assertTrue(context.getVariables().containsKey("versionPid"));
    }

    @Test
    public void testJBangCommandSaveOutput() {
        JBangAction jbang = new JBangAction.Builder().command("version")
                .saveOutput("out")
                .build();
        jbang.execute(context);

        Assertions.assertTrue(context.getVariables().containsKey("out"));
    }

    @Test
    public void testJBangCommandWithVariables() {
        JBangAction jbang = new JBangAction.Builder().command("${command}").build();
        context.setVariable("command", "version");

        jbang.execute(context);
    }

    @Test
    public void testJBangCommandValidateExitCode() {
        JBangAction jbang = new JBangAction.Builder().command("version").exitCodes(-1).build();
        ValidationException e = Assertions.assertThrows(ValidationException.class, () -> jbang.execute(context));
        Assertions.assertTrue(e.getMessage().contains("Expected exit code"));
    }

    @Test
    public void testUnknownApp() {
        JBangAction jbang = new JBangAction.Builder().app("unknown").command("version").build();
        Assertions.assertThrows(CitrusRuntimeException.class, () -> jbang.execute(context));
    }

    @Test
    public void testUnknownCommand() {
        JBangAction jbang = new JBangAction.Builder().command("unknown").build();
        Assertions.assertThrows(CitrusRuntimeException.class, () -> jbang.execute(context));
    }

}
