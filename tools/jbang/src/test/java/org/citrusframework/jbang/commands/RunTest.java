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
import org.testng.annotations.Test;

public class RunTest extends CommandTest {

    @Test
    public void shouldRunJavaTest() {
        Path source = Resources.fromClasspath("runnable/SampleIT.java").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunYamlTest() {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunXmlTest() {
        Path source = Resources.fromClasspath("runnable/xml-sample.citrus.it.xml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunGroovyTest() {
        Path source = Resources.fromClasspath("runnable/groovy-sample.citrus.it.groovy").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunCucumberTest() {
        Path source = Resources.fromClasspath("runnable/cucumber-sample.citrus.it.feature").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunAllTest() {
        Path source = Resources.fromClasspath("runnable").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }
}
