/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.citrus.integration.actions;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.AntRunAction.Builder.antrun;

/**
 * @author Christoph Deppisch
 */
@Test
public class AntRunActionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void antRunAction() {
        variable("welcomeText", "Hello Citrus today is citrus:currentDate()!");
        variable("checked", "true");

        run(antrun("classpath:org/citrusframework/citrus/integration/actions/build.xml")
            .target("sayHello"));

        run(antrun("classpath:org/citrusframework/citrus/integration/actions/build.xml")
            .targets("sayHello", "sayGoodbye"));

        run(antrun("classpath:org/citrusframework/citrus/integration/actions/build.xml")
            .target("sayHello")
            .property("welcomeText", "${welcomeText}")
            .property("goodbyeText", "Goodbye!"));

        run(antrun("classpath:org/citrusframework/citrus/integration/actions/build.xml")
            .target("checkMe")
            .propertyFile("classpath:org/citrusframework/citrus/integration/actions/build.properties"));
    }
}
