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

package org.citrusframework.integration.container;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Iterate.Builder.iterate;

/**
 * @author Christoph Deppisch
 */
@Test
public class IterateJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void iterateContainer() {
        variable("max", "3");

        run(iterate().condition("i lt= citrus:randomNumber(1)").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("i lt 20").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("(i lt 5) or (i = 5)").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("(i lt 5) and (i lt 3)").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("i = 0").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("${max} gt= i").index("i").actions(echo("index is: ${i}")));

        run(iterate().condition("i lt= 50").index("i")
                .startsWith(0)
                .step(5)
                .actions(echo("index is: ${i}")));

        AbstractTestAction anonymous = action(context -> logger.info(context.getVariable("index"))).build();

        run(iterate().condition("i lt 5").index("i")
                .actions(createVariable("index", "${i}"), () -> anonymous));
    }
}
