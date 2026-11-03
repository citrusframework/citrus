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

package org.citrusframework.jbang.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.jbang.actions.JBangAction;
import org.citrusframework.util.TestUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JBangTest extends AbstractXmlActionTest {

    @BeforeAll
    public static void beforeClass() {
        if (!TestUtils.isNetworkReachable()) {
            Assumptions.abort("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }
    }

    @Test
    public void shouldLoadJBangActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/jbang/xml/jbang.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("JBangTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());
        Assertions.assertEquals(JBangAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        JBangAction action = (JBangAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("version", action.getScriptOrFile());
        Assertions.assertEquals(1L, action.getArgs().size());
        Assertions.assertEquals("--verbose", action.getArgs().get(0));
        Assertions.assertTrue(action.isPrintOutput());

        action = (JBangAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getScriptOrFile().endsWith("hello.java"));
        Assertions.assertEquals(1L, action.getArgs().size());
        Assertions.assertEquals("Citrus", action.getArgs().get(0));
        Assertions.assertEquals("out", action.getOutputVar());
        Assertions.assertEquals("pid", action.getPidVar());
        Assertions.assertTrue(action.isPrintOutput());

        action = (JBangAction) result.getTestAction(actionIndex);
        Assertions.assertTrue(action.getScriptOrFile().endsWith("hello.java"));
        Assertions.assertEquals(1L, action.getArgs().size());
        Assertions.assertEquals("Citrus", action.getArgs().get(0));
        Assertions.assertTrue(action.getVerifyOutput().endsWith("Hello Citrus"));
        Assertions.assertArrayEquals(new int[]{0}, action.getExitCodes());
        Assertions.assertFalse(action.isPrintOutput());

        Assertions.assertTrue(context.getVariables().containsKey("pid"));
        Assertions.assertNotNull(context.getVariable("out"));
        Assertions.assertTrue(context.getVariable("out").endsWith("Hello Citrus"));
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("jbang").isPresent());
        Assertions.assertEquals(JBang.class, XmlTestActionBuilder.lookup("jbang").get().getClass());
    }
}
