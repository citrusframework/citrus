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

package org.citrusframework.jbang.engine;

import java.nio.file.Path;
import java.util.Collections;

import org.citrusframework.TestSource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JBangTestEngineTest {

    @Test
    public void testRunFromWorkDirectory() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        runTestEngine(configuration, Resources.fromClasspath("org/citrusframework/jbang/sample").getFile().toPath());
    }

    @Test
    public void testRunDirectory() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setPackages(Collections.singletonList(Resources.fromClasspath("org/citrusframework/jbang/sample").getFile().getAbsolutePath()));
        runTestEngine(configuration);
    }

    @Test
    public void testRunSource() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setTestSources(Collections.singletonList(new TestSource("yaml", "hello",
                Resources.fromClasspath("org/citrusframework/jbang/sample/hello.it.yaml").getFile().getAbsolutePath())));

        runTestEngine(configuration);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRunNoMatch() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setPackages(Collections.singletonList("some.foo.directory"));
        runTestEngine(configuration);
    }

    @Test
    public void shouldResolveJBangEngine() {
        TestRunConfiguration configuration = new TestRunConfiguration();
        configuration.setEngine("jbang");
        TestEngine engine = TestEngine.lookup(configuration);

        Assert.assertEquals(engine.getClass(), JBangTestEngine.class);
    }

    private void runTestEngine(TestRunConfiguration configuration, Path workDir) {
        JBangTestEngine engine = new JBangTestEngine(configuration);
        engine.withOutputListener(System.out::print);
        engine.withWorkingDir(workDir);
        engine.run();
    }

    private void runTestEngine(TestRunConfiguration configuration) {
        JBangTestEngine engine = new JBangTestEngine(configuration);
        engine.withOutputListener(System.out::print);
        engine.run();
    }
}
