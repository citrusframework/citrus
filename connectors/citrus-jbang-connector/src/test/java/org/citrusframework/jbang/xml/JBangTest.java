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
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JBangTest extends AbstractXmlActionTest {

    @BeforeClass
    public static void beforeClass() {
        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }
    }

    @Test
    public void shouldLoadJBangActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/jbang/xml/jbang-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "JBangTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), JBangAction.class);

        int actionIndex = 0;

        JBangAction action = (JBangAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getScriptOrFile(), "version");
        Assert.assertEquals(action.getArgs().size(), 1L);
        Assert.assertEquals(action.getArgs().get(0), "--verbose");
        Assert.assertTrue(action.isPrintOutput());

        action = (JBangAction) result.getTestAction(actionIndex++);
        Assert.assertTrue(action.getScriptOrFile().endsWith("hello.java"));
        Assert.assertEquals(action.getArgs().size(), 1L);
        Assert.assertEquals(action.getArgs().get(0), "Citrus");
        Assert.assertEquals(action.getOutputVar(), "out");
        Assert.assertEquals(action.getPidVar(), "pid");
        Assert.assertTrue(action.isPrintOutput());

        action = (JBangAction) result.getTestAction(actionIndex);
        Assert.assertTrue(action.getScriptOrFile().endsWith("hello.java"));
        Assert.assertEquals(action.getArgs().size(), 1L);
        Assert.assertEquals(action.getArgs().get(0), "Citrus");
        Assert.assertTrue(action.getVerifyOutput().endsWith("Hello Citrus"));
        Assert.assertEquals(action.getExitCodes(), new int[]{0});
        Assert.assertFalse(action.isPrintOutput());

        Assert.assertTrue(context.getVariables().containsKey("pid"));
        Assert.assertNotNull(context.getVariable("out"));
        Assert.assertTrue(context.getVariable("out").endsWith("Hello Citrus"));
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(XmlTestActionBuilder.lookup("jbang").isPresent());
        Assert.assertEquals(XmlTestActionBuilder.lookup("jbang").get().getClass(), JBang.class);
    }
}
